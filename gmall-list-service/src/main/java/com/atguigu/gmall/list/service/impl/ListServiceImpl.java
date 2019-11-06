package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay
 * @create 2019-11-02 16:51
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 商品上架，从数据库中查询数据并赋值给SkuLsInfo
     * @param skuLsInfo
     */
    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        /**
         * 定义并执行PUT Index/Type/1 ---> gmall/SkuInfo/1
         */
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        // 1定义语句
        // 2定义，执行动作
        // 3得到结果，返回结果
        String query = makeQueryStringForSearch(skuLsParams);

        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams, searchResult);

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();

        String hotKey = "hotScore";

        Double hotScore = jedis.zincrby(hotKey, 1, "skuId:" + skuId);
        // 每当hotScore是10的倍数时，更新es
        if (hotScore % 10 == 0) {
            updateHotScore(skuId,  Math.round(hotScore));
        }

    }

    /**
     * 更新es
     * @param skuId
     * @param hotScore
     */
    private void updateHotScore(String skuId, long hotScore) {

        String upd="{\n" +
                "  \"doc\": {\n" +
                "      \"hotScore\": "+hotScore+"\n" +
                "  }\n" +
                "}";

        Update update = new Update.Builder(upd).index(ES_INDEX).type(ES_TYPE).id(skuId).build();

        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据用户输入的条件，动态生成 dsl语句
     * @param skuLsParams
     * @return
     */
    public String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        // 定义查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // query --> bool (QueryBuilder)
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 3.通过keyword进行检索
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            // bool --> must --> match
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

            // 当有关键字的时候，要设置高亮 highlighter query平级
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
            highlighter.preTags("<span style=color:red>");
            highlighter.field("skuName");
            highlighter.postTags("</span>");
            // 将高亮对象放入查询器
            searchSourceBuilder.highlight(highlighter);

        }
        // 1.判断是否有三级分类 Id，若有则根据catalogId过滤
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            // bool --> filter --> term
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // 2.判断平台属性 id
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (String valueId : skuLsParams.getValueId()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                // bool --> filter --> term
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        // 定义query
        searchSourceBuilder.query(boolQueryBuilder);

        // 4.设置分页
        // from = (pageNum-1)*pageSize
        int from = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        // 5.排序 (降序)
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        // 6.聚合
        // aggs --> groupby_attr --> terms --> field
        TermsBuilder groupby_att = AggregationBuilders.terms("groupby_att").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_att);

        String query = searchSourceBuilder.toString();
        System.out.println("生成后的query：" + query);

        return query;
    }

    /**
     * 数据结果集的转换
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    public SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {

        SkuLsResult skuLsResult = new SkuLsResult();

        // 1.SkuLsResult中的 List<SkuLsInfo> skuLsInfoList;属性
        List<SkuLsInfo> arrayList = new ArrayList<>();
        // 通过 dsl语句查询出来的所有数据都存储在 searchResult中
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);

        if (hits != null && hits.size() > 0) {
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo skuLsInfo = hit.source;

                // 2.将skuName的高亮替换SkuInfo中的skuName
                if (hit.highlight != null && hit.highlight.size() > 0) {
                    List<String> skuName = hit.highlight.get("skuName");
                    // skuName集合中的第一个元素赋值给skuLsInfo中的skuName
                    skuLsInfo.setSkuName(skuName.get(0));
                }
                arrayList.add(skuLsInfo);
            }
        }

        skuLsResult.setSkuLsInfoList(arrayList);

        // 3.设置查询到的总数 long total;
        skuLsResult.setTotal(searchResult.getTotal());

        // 4.总页数  long totalPages;
        // 公式：总页数 = (总数 + 每页最大个数 - 1) / 每页最大个数
        long totalPages = (searchResult.getTotal() + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);

        // 5.存储平台属性 id(聚合中获取bucket)   List<String> attrValueIdList;
        // 存储平台属性id集合
        ArrayList<String> stringArrayList = new ArrayList<>();
        // 通过聚合来获取平台属性 id
        MetricAggregation aggregations = searchResult.getAggregations();
        // 通过聚合的名称groupby_att 获取数据对象
        TermsAggregation groupby_att = aggregations.getTermsAggregation("groupby_att");
        // 通过groupby_att 获取 buckets
        List<TermsAggregation.Entry> buckets = groupby_att.getBuckets();
        // 循环遍历集合获取key！
        if (buckets != null && buckets.size() > 0) {
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                stringArrayList.add(valueId);
            }
        }

        skuLsResult.setAttrValueIdList(stringArrayList);
        System.out.println("skuLsResult" + JSON.toJSONString(skuLsResult));

        return skuLsResult;
    }


}
