package com.deepxq.dvcndsi.x.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;
import com.deepxq.dvcndsi.x.service.ProductService;
import com.deepxq.dvcndsi.x.domain.eo.Product;
import com.deepxq.dvcndsi.x.extension.ApplicationException;
import com.deepxq.dvcndsi.x.extension.AppRuntimeEnv;
import com.deepxq.dvcndsi.x.mapper.ProductMapper;
import com.deepexi.util.pageHelper.PageBean;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import javax.ws.rs.core.Response;

@Component
@Service(version = "${demo.service.version}")
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AppRuntimeEnv appRuntimeEnv;

    public PageBean getProductList(Integer page, Integer size, Integer age) {
        PageHelper.startPage(page, size);
        List<Product> userTasks = productMapper.selectPageVo(age);
        return new PageBean<>(userTasks);
    }

    public Integer createProduct(Product product) {
        return productMapper.insert(product);
    }

    public Boolean deleteProductById(String id) {
        productMapper.deleteById(id);
        return true;
    }

    @SentinelResource(value = "testSentinel", fallback = "doFallback", blockHandler = "exceptionHandler")
    public Product getProductById(String id) {
        // dubbo生产者被消费者调用时，客户端隐式传入的参数
        String tenantId = RpcContext.getContext().getAttachment("tenantId");
        logger.info("获取客户端隐式参数，tenantId：{}", tenantId);
        return productMapper.selectById(id);
    }

    public String doFallback(long i) {
        // Return fallback value.
        return "Oops, degraded";
    }

    /**
     * 熔断降级处理逻辑
     * @param s
     * @param ex
     * @return
     */
    public void exceptionHandler(long s, Exception ex) {
        // Do some log here.
        logger.info("-------------熔断降级处理逻辑---------\n");
        throw new ApplicationException(Response.Status.BAD_REQUEST, "100001", "熔断降级处理!");
    }

    public void testError() {
        throw new ApplicationException(Response.Status.BAD_REQUEST, "100002", "这是返回的自定义错误信息!");
    }
}