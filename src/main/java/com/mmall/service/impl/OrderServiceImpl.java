package com.mmall.service.impl;

import com.alipay.demo.trade.model.result.AlipayF2FRefundResult;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.*;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.utils.ZxingUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;


import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zhonglunsheng on 2018/1/10.
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService{

    private static  AlipayTradeService tradeService;

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServiceResponse createOrder(Integer userId, Integer shippingId){
        //获取用户购物车信息
        List<Cart> cartList = cartMapper.selectCheckByUserId(userId);
        if (cartList == null){
            return ServiceResponse.createByErrorMessage("购物车获取出错");
        }
        ServiceResponse response = this.getOrderItemList(userId,cartList);
        if (!response.isSuccess()){
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();

        BigDecimal payment = this.getOrderTotalPrice(orderItemList);

        Order order = this.assembleOrder(userId,shippingId,payment);
        if (order == null){
            return ServiceResponse.createByErrorMessage("订单生成失败");
        }
        if (CollectionUtils.isEmpty(orderItemList)){
            return ServiceResponse.createByErrorMessage("购物车为空");
        }

        for (OrderItem orderItem:
             orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }

        //批量插入订单子表
        orderItemMapper.batchInsert(orderItemList);
        //减少产品库存
        this.reductProductStock(orderItemList);

        //清空购物车
        this.removeCart(cartList);

        order = orderMapper.selectByOrderNo(order.getOrderNo());
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return ServiceResponse.createBySuccess(orderVo);
    }

    /**
     * 取消订单
     * @param orderNo
     * @return
     */
    @Override
    public ServiceResponse cancelOrder(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServiceResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServiceResponse.createByErrorMessage("该订单已支付不能取消");
        }
        order.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int rowCount = orderMapper.updateByPrimaryKey(order);
        if (rowCount > 0){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }

    /**
     * 获取订单商品信息
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServiceResponse getOrderProduct(Integer userId, Long orderNo){
        OrderProductVo orderProductVo = new OrderProductVo();

        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,orderNo);
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        BigDecimal totalPrice = new BigDecimal("0");

        for (OrderItem orderItem:
             orderItemList) {
            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),orderItemVo.getTotalPrice().doubleValue());
            orderItemVoList.add(orderItemVo);
        }
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setProductTotalPrice(totalPrice);
        return ServiceResponse.createBySuccess(orderProductVo);
    }

    /**
     * 获取订单详情
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServiceResponse getOrderDetail(Integer userId, Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServiceResponse.createByErrorMessage("没有找到该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,orderNo);
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return ServiceResponse.createBySuccess(orderVo);
    }

    @Override
    public ServiceResponse getOrderList(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.getOrderListByUserId(userId);
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,userId);
        pageInfo.setList(orderVoList);
        return ServiceResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServiceResponse managerSearchOrder(Long orderNo, int pageNum, int pageSize){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null){
            PageHelper.startPage(pageNum,pageSize);
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
            OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
            pageInfo.setList(Lists.newArrayList(orderVo));
            return ServiceResponse.createBySuccess(pageInfo);
        }
        return ServiceResponse.createByErrorMessage("订单不存在");
    }


    @Override
    public ServiceResponse managerSendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServiceResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.SHIPPED.getCode()){
            return ServiceResponse.createByErrorMessage("该订单已发货,请勿重复操作");
        }
        order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
        int rowCount = orderMapper.updateByPrimaryKey(order);
        if (rowCount > 0){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }

    @Override
    public void closeOrder(int hour){
        Date closeTime = DateUtils.addHours(new Date(),-hour);
        List<Order> orderList = orderMapper.getOrderStatusByCreatetime(Const.OrderStatusEnum.NO_PAY.getCode(),DateTimeUtil.dateToStr(closeTime));

        for (Order order:
             orderList) {
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());

            for (OrderItem orderItem:
                 orderItemList) {
                Integer stock = productMapper.getProductStockById(orderItem.getProductId());
                //商品不存在直接跳过
                if (stock == null){
                    continue;
                }
                Product product = new Product();
                product.setId(orderItem.getProductId());
                product.setStock(stock + orderItem.getQuantity());
                productMapper.updateByPrimaryKeySelective(product);
            }

            orderMapper.closeOrderByOrderId(order.getId());
            log.info("关闭订单OrderNo:{}",order.getOrderNo());
        }

    }
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order:
             orderList) {
            List<OrderItem> orderItemList  =  orderItemMapper.selectByUserId(userId,order.getOrderNo());
            OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }
    /**
     * 获取订单子表
     * @param userId
     * @param cartList
     * @return
     */
    private ServiceResponse getOrderItemList(Integer userId,List<Cart> cartList){
        if (CollectionUtils.isEmpty(cartList)){
            return ServiceResponse.createByErrorMessage("购物车为空");
        }

        List<OrderItem> orderItemList = Lists.newArrayList();
        for (Cart cartItem:
             cartList) {
            //检查购物车状态
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (Const.ProductStateEnum.ON_SALE.getStatus() != product.getStatus()){
                return ServiceResponse.createByErrorMessage(product.getName()+"商品已下架");
            }
            if (cartItem.getQuantity() > product.getStock()){
                return ServiceResponse.createByErrorMessage("商品库存不足");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServiceResponse.createBySuccess(orderItemList);
    }

    /**
     * 获取订单金额
     * @param orderItemList
     * @return
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal orderTotalPrice = new BigDecimal("0");
        for (OrderItem orderItem: orderItemList) {
            orderTotalPrice = BigDecimalUtil.add(orderTotalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return orderTotalPrice;
    }

    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @param payment
     * @return
     */
    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order = new Order();
        long orderNo =  this.generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPayment(payment);
        order.setShippingId(shippingId);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);

        int rowCount = orderMapper.insert(order);
        if (rowCount > 0){
            return order;
        }
        return null;
    }

    /**
     * 生成订单号
     * @return
     */
    private Long generateOrderNo(){
        long currentTime = System.currentTimeMillis();
        return currentTime+new Random().nextInt(100);
    }

    /**
     * 减少商品库存
     * @param orderItemList
     */
    private void reductProductStock(List<OrderItem> orderItemList){
        for (OrderItem orderItem:
             orderItemList) {
            productMapper.updateStockByProductIdAndQuantity(orderItem.getId(),orderItem.getQuantity());
        }
    }

    /**
     * 清空购物车
     * @param cartList
     */
    private void removeCart(List<Cart> cartList){
        for (Cart cart:
             cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));


        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    @Override
    public ServiceResponse pay(Long orderNo, Integer userId, String path){
        Map<String,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order == null){
            return ServiceResponse.createByErrorMessage("用户没有该订单");
        }
        resultMap.put("orderNo",order.getOrderNo().toString());


        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "happy商城购物";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuffer("购买商品共").append(order.getPayment()).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();


        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,orderNo);
        for (OrderItem item:
             orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods1 = GoodsDetail.newInstance(item.getProductId().toString(), item.getProductName(),
                    BigDecimalUtil.mul(item.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(), item.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods1);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                String qrPath = String.format(path+"/qr-%s.png",response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());

                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path,qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (Exception e) {
                    log.error("二维码上传异常");
                    return ServiceResponse.createByErrorMessage("二维码上传异常");
                }

                // 需要修改为运行机器上的路径
                log.info("qrPath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("qrUrl",qrUrl);
                return ServiceResponse.createBySuccess(resultMap);
            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServiceResponse.createBySuccess("支付宝预下单失败!!!");
            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServiceResponse.createBySuccess("系统异常，预下单状态未知!!!");
            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServiceResponse.createBySuccess("不支持的交易状态，交易返回异常!!!");
        }
    }

    @Override
    public ServiceResponse aliCallback(Map<String, String> params){
        //验证其他数据
        //out_trade_no是否为商户系统中创建的订单号
        //并判断total_amount是否确实为该订单的实际金额
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String payment = params.get("total_amount");
        //支付状态
        String tradeStatus = params.get("trade_status");
        //交易号
        String tradeNo = params.get("trade_no");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServiceResponse.createByErrorMessage("订单不存在,回调忽略");
        }
        if (!order.getPayment().toString().equals(payment)){
            return ServiceResponse.createByErrorMessage("订单支付金额不对,回调忽略");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAY.getCode()){
            return ServiceResponse.createByErrorMessage("支付宝重复调用");
        }
        //检查交易状态并更新订单
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAY.getCode());
            orderMapper.updateByPrimaryKey(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServiceResponse.createBySuccess();
    }

    @Override
    public ServiceResponse aliRefund(Integer userId, Long orderNo) {
        // (必填) 外部订单号，需要退款交易的商户外部订单号
        String outTradeNo = orderNo.toString();

        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order == null){
            return ServiceResponse.createByErrorMessage("用户没有该订单,无法退款");
        }

        // (必填) 退款金额，该金额必须小于等于订单的支付金额，单位为元
        String refundAmount = order.getPayment().toString();

        // (可选，需要支持重复退货时必填) 商户退款请求号，相同支付宝交易号下的不同退款请求号对应同一笔交易的不同退款申请，
        // 对于相同支付宝交易号下多笔相同商户退款请求号的退款交易，支付宝只会进行一次退款
        String outRequestNo = "";

        // (必填) 退款原因，可以说明用户退款原因，方便为商家后台提供统计
        String refundReason = "正常退款，用户买多了";

        // (必填) 商户门店编号，退款情况下可以为商家后台提供退款权限判定和统计等作用，详询支付宝技术支持
        String storeId = "test_store_id";

        // 创建退款请求builder，设置请求参数
        AlipayTradeRefundRequestBuilder builder = new AlipayTradeRefundRequestBuilder()
                .setOutTradeNo(outTradeNo).setRefundAmount(refundAmount).setRefundReason(refundReason)
                .setOutRequestNo(outRequestNo).setStoreId(storeId);

        AlipayF2FRefundResult result = tradeService.tradeRefund(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝退款成功: )");
                return ServiceResponse.createBySuccess("支付宝退款成功: )");

            case FAILED:
                log.error("支付宝退款失败!!!");
                return ServiceResponse.createByErrorMessage("支付宝退款失败!!!");

            case UNKNOWN:
                log.error("系统异常，订单退款状态未知!!!");
                return ServiceResponse.createByErrorMessage("系统异常，订单退款状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServiceResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    @Override
    public ServiceResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServiceResponse.createByErrorMessage("用户没有该订单,无法退款");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAY.getCode()){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }


}
