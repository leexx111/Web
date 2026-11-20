package com.cms.electricityproject.config;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.service.Service;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyMultipleRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.*;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.RequestUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component // 声明为Spring组件，由Spring管理生命周期
/**
 * 封装 BACnet 设备发现、点位读取及 DI 报警采集流程。
 */
public class BacnetTool {

    private LocalDevice localDevice;
    private final Map<Integer, RemoteDevice> discoveredDevices = new ConcurrentHashMap<>();
    private CountDownLatch discoveryLatch;

    /**
     * 初始化本地设备（适配6.0.0）
     * 项目启动时启动
     */

    @PostConstruct // 项目启动时自动调用
    /**
     * 初始化本地 BACnet 设备与网络传输，并在应用启动时发现网关。
     */
    public void init() throws Exception {
        IpNetwork network = new IpNetworkBuilder()
//                .withLocalBindAddress("192.168.1.227")
                .withPort(47808)
                .withReuseAddress(true)
//                .withBroadcast("255.255.255.0",24) // 关键修改：添加广播地址
                .withLocalBindAddress("0.0.0.0")    // 监听所有接口
                .withBroadcast("192.168.1.255",24) // 关键修改：添加广播地址
                .build();

        DefaultTransport transport = new DefaultTransport(network);
        localDevice = new LocalDevice(105, transport);
        localDevice.initialize();
        System.out.println("本地设备初始化成功，ID: " + 105);

        this.discoverDevices(5); // 5秒超时
    }
    /**
     * 发现网络中的BACnet设备（带同步等待）
     */
    public void discoverDevices(int timeoutSeconds) throws Exception {
        // 清除已有设备列表（避免重复添加）
        discoveredDevices.clear();
        
        localDevice.getEventHandler().addListener(new DeviceEventListener() {
            @Override
            public void listenerException(Throwable throwable) {

            }

            @Override
            public void iAmReceived(RemoteDevice remoteDevice) {
                discoveredDevices.put(remoteDevice.getInstanceNumber(), remoteDevice);
                System.out.println("发现设备: " + remoteDevice.getInstanceNumber());
                discoveryLatch.countDown(); // 发现设备后释放锁
            }

            @Override
            public boolean allowPropertyWrite(Address address, BACnetObject baCnetObject, PropertyValue propertyValue) {
                return false;
            }

            @Override
            public void propertyWritten(Address address, BACnetObject baCnetObject, PropertyValue propertyValue) {

            }

            @Override
            public void iHaveReceived(RemoteDevice remoteDevice, RemoteObject remoteObject) {

            }

            @Override
            public void covNotificationReceived(UnsignedInteger unsignedInteger, ObjectIdentifier objectIdentifier, ObjectIdentifier objectIdentifier1, UnsignedInteger unsignedInteger1, SequenceOf<PropertyValue> sequenceOf) {

            }

            @Override
            public void eventNotificationReceived(UnsignedInteger unsignedInteger, ObjectIdentifier objectIdentifier, ObjectIdentifier objectIdentifier1, TimeStamp timeStamp, UnsignedInteger unsignedInteger1, UnsignedInteger unsignedInteger2, EventType eventType, CharacterString characterString, NotifyType notifyType, Boolean aBoolean, EventState eventState, EventState eventState1, NotificationParameters notificationParameters) {

            }

            @Override
            public void textMessageReceived(ObjectIdentifier objectIdentifier, Choice choice, MessagePriority messagePriority, CharacterString characterString) {

            }

            @Override
            public void synchronizeTime(Address address, DateTime dateTime, boolean b) {

            }

            @Override
            public void requestReceived(Address address, Service service) {

            }


        });

        localDevice.sendGlobalBroadcast(new WhoIsRequest());
        System.out.println("开始设备发现，等待" + timeoutSeconds + "秒...");
//        Thread.sleep(1000);
        Thread.sleep(timeoutSeconds * 1000);
//        if (discoveryLatch.await(timeoutSeconds, TimeUnit.SECONDS)) {
            System.out.println("发现设备完成，共找到" + discoveredDevices.size() + "个设备");
//        } else {
//            System.out.println("设备发现超时，找到" + discoveredDevices.size() + "个设备");
//        }
    }

    /**
     * 读取设备属性（通过设备ID）
     */
    public void readProperty(int targetDeviceId, ObjectType objectType, 
                            int objectInstance, PropertyIdentifier property) throws Exception {
        RemoteDevice remoteDevice = discoveredDevices.get(targetDeviceId);
        if (remoteDevice == null) {
            throw new RuntimeException("未找到设备ID: " + targetDeviceId);
        }

        ObjectIdentifier oid = new ObjectIdentifier(objectType, objectInstance);
        Object result = RequestUtils.getProperty(localDevice, remoteDevice, oid, property);
        System.out.println("设备 " + targetDeviceId + " 属性 " + property + ": " + result);
    }
    /**
     * 读取对象的属性
     */
    public  Map.Entry readObjectProperties(int targetDeviceId, ObjectType objectType, int objectInstance) throws Exception {
        RemoteDevice remoteDevice = discoveredDevices.get(targetDeviceId);
        if (remoteDevice == null) {
            throw new RuntimeException("未找到设备ID: " + targetDeviceId);
        }
        // 构建对象标识符
        ObjectIdentifier oid = new ObjectIdentifier(objectType, objectInstance);
        //批量读取
//        Encodable property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.objectList);
        // 读取对象名称
        Object objectName =  RequestUtils.getProperty(
                localDevice, remoteDevice, oid, PropertyIdentifier.objectName);
        Object presentValue ="-";
        // 读取当前值（如果对象支持）
        try {
             presentValue = RequestUtils.getProperty(localDevice, remoteDevice, oid, PropertyIdentifier.presentValue);
//            System.out.println("对象 " + oid + " 的当前值: " + presentValue);
        } catch (Exception e) {
            System.out.println("对象 " + oid + " 不支持 presentValue 属性");
        }
        return new java.util.AbstractMap.SimpleEntry<>(objectName, presentValue);
    }



    /**
     * 关闭设备连接
     */
    @PreDestroy // 项目关闭时自动调用
    public void shutdown() {
        if (localDevice != null) {
            localDevice.terminate();
            System.out.println("设备连接已关闭");
        }
    }

    /**
     * 获取设备的对象列表
     * @param targetDeviceId
     * @return
     * @throws Exception
     */
    public List<ObjectIdentifier> getObjectList(int targetDeviceId) throws Exception {
        RemoteDevice remoteDevice = discoveredDevices.get(targetDeviceId);
        if (remoteDevice == null) {
            throw new RuntimeException("未找到设备ID: " + targetDeviceId);
        }
        // 读取Device对象的objectList属性
        SequenceOf<ObjectIdentifier> objectList = RequestUtils.getObjectList(localDevice, remoteDevice);
//        ReadPropertyMultipleRequest request = new ReadPropertyMultipleRequest();

        return objectList.getValues();
    }

    /**
     * 启动时调用，连接设备
     *
     */
    public static void MyRuninit(){
    }

    /**
     * 外部接口访问，获取所有的值
     * @param
     */

//    @Async("asyncTaskExecutor")
    /**
     * 按 Object_List 筛选并分段读取 AI 对象，单点失败后短暂重试并继续扫描。
     */
    private void readAnalogInputsFromObjectList(Map<Object, Object> target, int gateway) throws Exception {
        int readCount = 0;
        int failedCount = 0;
        List<ObjectIdentifier> analogInputs = new ArrayList<>();
        for (ObjectIdentifier objectIdentifier : getObjectList(gateway)) {
            if (!ObjectType.analogInput.equals(objectIdentifier.getObjectType())) {
                continue;
            }
            analogInputs.add(objectIdentifier);
        }
        analogInputs.sort(Comparator.comparingInt(ObjectIdentifier::getInstanceNumber));
        for (ObjectIdentifier objectIdentifier : analogInputs) {
            Map.Entry point;
            try {
                point = readObjectProperties(
                        gateway, objectIdentifier.getObjectType(), objectIdentifier.getInstanceNumber());
            } catch (Exception firstFailure) {
                try {
                    Thread.sleep(200);
                    point = readObjectProperties(
                            gateway, objectIdentifier.getObjectType(), objectIdentifier.getInstanceNumber());
                } catch (Exception retryFailure) {
                    failedCount++;
                    System.err.println("BACnet AI读取重试后仍失败，继续扫描: gateway=" + gateway
                            + ", object=" + objectIdentifier + ", error="
                            + retryFailure.getClass().getSimpleName());
                    continue;
                }
            }
            target.put(point.getKey(), point.getValue());
            readCount++;
        }
        System.out.println("网关" + gateway + "按Object_List读取AI对象数=" + readCount
                + ",失败数=" + failedCount);
    }

    /**
     * 采集常规配电网关的模拟量点位并汇总为名称和值映射。
     */
    public  Map<Object,Object> Hqsj(){
        Map<Object, Object> maps = new LinkedHashMap<>();
        try {
            // 1. 检查连接是否已初始化
            if (localDevice == null || !localDevice.isInitialized()) {
                throw new RuntimeException("BACnet连接未初始化，请检查启动日志");
            }

            if (discoveredDevices.isEmpty()) {
                System.out.println("未发现设备，重新执行发现...");
                this.discoverDevices(5); // 重新发现设备（5秒超时）
            }

            // 读取第一个发现的设备属性（示例）
            if (discoveredDevices.size() >= 8) {
//                int firstDeviceId = tools.discoveredDevices.keySet().iterator().next();  读取第一个设备id
                int firstDeviceId = 301;
                int towDeviceId = 302;
                int threeDeviceId = 303;
                int fourDeviceId = 304;
                int fiveDeviceId = 305;
                int sixDeviceId = 306;
                int sevenDeviceId = 307;
                int eightDeviceId = 308;

                // 读取第一个对象的属性（示例：读取第一个 Analog Input）1
                readAnalogInputsFromObjectList(maps, firstDeviceId);
                System.out.println("设备1读取完成");
                readAnalogInputsFromObjectList(maps, towDeviceId);
                System.out.println("设备2读取完成");
                readAnalogInputsFromObjectList(maps, threeDeviceId);
                System.out.println("设备3读取完成");
                readAnalogInputsFromObjectList(maps, fourDeviceId);
                System.out.println("设备4读取完成");
                readAnalogInputsFromObjectList(maps, fiveDeviceId);

                System.out.println("设备5读取完成");
                readAnalogInputsFromObjectList(maps, sixDeviceId);

                System.out.println("设备6读取完成");
                readAnalogInputsFromObjectList(maps, eightDeviceId);

                System.out.println("设备8读取完成");
                readAnalogInputsFromObjectList(maps, sevenDeviceId);

                System.out.println("设备7读取完成");
                // 输出存储的键值对-
//                for (Map.Entry<Object, Object> entry : maps.entrySet()) {
//                    if (entry.getKey().toString().equals("M3.Ia")){
//                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
//                    }
//                }
                if(discoveredDevices.containsKey(309)  ){
                    System.out.println("设备中存在309的健-----------");
                    readAnalogInputsFromObjectList(maps, 309);

                    System.out.println("309读取完毕-----------");
                }

                if (discoveredDevices.containsKey(312)){
                    System.out.println("存在312的值");
                    readAnalogInputsFromObjectList(maps, 312);
                    System.out.println("312读取完毕");
                }
                if (discoveredDevices.containsKey(313)){
                    System.out.println("存在313的值");
                    readAnalogInputsFromObjectList(maps, 313);
                    System.out.println("313读取完毕");
                }
                if (discoveredDevices.containsKey(314)){
                    System.out.println("存在314的值");
                    readAnalogInputsFromObjectList(maps, 314);
                    System.out.println("314读取完毕");
                }
                if (discoveredDevices.containsKey(315)){
                    System.out.println("存在315的值");
                    readAnalogInputsFromObjectList(maps, 315);
                    System.out.println("315读取完毕");
                }
                if (discoveredDevices.containsKey(316)){
                    System.out.println("存在316的值");
                    readAnalogInputsFromObjectList(maps, 316);
                    System.out.println("316读取完毕");
                }
                if (discoveredDevices.containsKey(317)){
                    System.out.println("存在317的值");
                    readAnalogInputsFromObjectList(maps, 317);
                    System.out.println("317读取完毕");
                }
                if (discoveredDevices.containsKey(318)){
                    System.out.println("存在318的值");
                    readAnalogInputsFromObjectList(maps, 318);
                    System.out.println("318读取完毕");
                }
                if (discoveredDevices.containsKey(325)){
                    System.out.println("存在325的值");
                    readAnalogInputsFromObjectList(maps, 325);
                    System.out.println("325读取完毕");
                }
                if (discoveredDevices.containsKey(322)){
                    System.out.println("存在322的值");
                    readAnalogInputsFromObjectList(maps, 322);
                    System.out.println("322读取完毕");
                }
//新增散表统计
                //1筒 361 4个点
//                if (discoveredDevices.containsKey(361)){
//                    System.out.println("存在361的值");
//                    for (int i =0;i<=3;i++){
//                        Map.Entry entry = readObjectProperties(361, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("361读取完毕");
//                }
//                //2筒 362 7
//                if (discoveredDevices.containsKey(362)){
//                    System.out.println("存在362的值");
//                    for (int i =0;i<=6;i++){
//                        Map.Entry entry = readObjectProperties(362, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("362读取完毕");
//                }
//                //3筒 363 10个
//                if (discoveredDevices.containsKey(363)){
//                    System.out.println("存在363的值");
//                    for (int i =0;i<=9;i++){
//                        Map.Entry entry = readObjectProperties(363, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("363读取完毕");
//                }
//                //4筒 364 5个
//                if (discoveredDevices.containsKey(364)){
//                    System.out.println("存在364的值");
//                    for (int i =0;i<=4;i++){
//                        Map.Entry entry = readObjectProperties(364, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("364读取完毕");
//                }
//                //6筒 366 21
//                if (discoveredDevices.containsKey(366)){
//                    System.out.println("存在366的值");
//                    for (int i =0;i<=20;i++){
//                        Map.Entry entry = readObjectProperties(366, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("366读取完毕");
//                }
//                //7筒 367 3
//                if (discoveredDevices.containsKey(367)){
//                    System.out.println("存在367的值");
//                    for (int i =0;i<=2;i++){
//                        Map.Entry entry = readObjectProperties(367, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("367读取完毕");
//                }


                return maps;
            } else {
                System.out.println("发现设备数量不足-");
                return maps;
            }



        } catch (Exception e) {
            e.printStackTrace();
            return maps;
        }
    }
    /**
     * 读取散表及相关网关的电气数据并统一返回点位映射。
     */
    public  Map<Object,Object> Hqsbsj(){
        Map<Object, Object> maps = new LinkedHashMap<>();
        try {
            // 1. 检查连接是否已初始化
            if (localDevice == null || !localDevice.isInitialized()) {
                throw new RuntimeException("BACnet连接未初始化，请检查启动日志");
            }

            if (discoveredDevices.isEmpty()) {
                System.out.println("未发现设备，重新执行发现...");
                this.discoverDevices(5); // 重新发现设备（5秒超时）
            }

            // 读取第一个发现的设备属性（示例）
            if (discoveredDevices.size() >= 8) {
////                int firstDeviceId = tools.discoveredDevices.keySet().iterator().next();  读取第一个设备id
//                int firstDeviceId = 301;
//                int towDeviceId = 302;
//                int threeDeviceId = 303;
//                int fourDeviceId = 304;
//                int fiveDeviceId = 305;
//                int sixDeviceId = 306;
//                int sevenDeviceId = 307;
//                int eightDeviceId = 308;
//
//                // 读取第一个对象的属性（示例：读取第一个 Analog Input）1
//                for (int i =0;i<= 847;i++){
//                    Map.Entry entry = readObjectProperties(firstDeviceId, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                }
//                System.out.println("设备1读取完成");
//                for (int i =0;i<= 980;i++){
//                    Map.Entry entry = readObjectProperties(towDeviceId, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                }
//                System.out.println("设备2读取完成");
//                for (int i =0;i<= 893;i++){
//                    Map.Entry entry = readObjectProperties(threeDeviceId, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                }
//                System.out.println("设备3读取完成");
//                for (int i =0;i<= 989;i++){
//                    Map.Entry entry = readObjectProperties(fourDeviceId, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                }
//                System.out.println("设备4读取完成");
//                for (int i =0;i<= 1015;i++){
//                    Map.Entry entry = readObjectProperties(fiveDeviceId, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                }
//
//                System.out.println("设备5读取完成");
//                for (int i =0;i<= 989;i++){
//                    Map.Entry entry = readObjectProperties(sixDeviceId, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                }
//
//                System.out.println("设备6读取完成");
//                for (int i =0;i<= 1019;i++){
//                    Map.Entry entry = readObjectProperties(eightDeviceId, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                }
//
//                System.out.println("设备8读取完成");
//                for (int i =0;i<= 1019;i++){
//                    Map.Entry entry = readObjectProperties(sevenDeviceId, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                }
//
//                System.out.println("设备7读取完成");
//                // 输出存储的键值对-
////                for (Map.Entry<Object, Object> entry : maps.entrySet()) {
////                    if (entry.getKey().toString().equals("M3.Ia")){
////                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
////                    }
////                }
//                if(discoveredDevices.containsKey(309)  ){
//                    System.out.println("设备中存在309的健-----------");
//                    for (int i =60;i<= 537;i++){
//                        Map.Entry entry = readObjectProperties(309, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//
//                    System.out.println("309读取完毕-----------");
//                }
//
//                if (discoveredDevices.containsKey(312)){
//                    System.out.println("存在312的值");
//                    for (int i =0;i<=869;i++){
//                        Map.Entry entry = readObjectProperties(312, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("312读取完毕");
//                }
//                if (discoveredDevices.containsKey(313)){
//                    System.out.println("存在313的值");
//                    for (int i =0;i<=999;i++){
//                        Map.Entry entry = readObjectProperties(313, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("313读取完毕");
//                }
//                if (discoveredDevices.containsKey(314)){
//                    System.out.println("存在314的值");
//                    for (int i =0;i<=1012;i++){
//                        Map.Entry entry = readObjectProperties(314, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("314读取完毕");
//                }
//                if (discoveredDevices.containsKey(315)){
//                    System.out.println("存在315的值");
//                    for (int i =0;i<=1015;i++){
//                        Map.Entry entry = readObjectProperties(315, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("315读取完毕");
//                }
//                if (discoveredDevices.containsKey(316)){
//                    System.out.println("存在316的值");
//                    for (int i =0;i<=954;i++){
//                        Map.Entry entry = readObjectProperties(316, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("316读取完毕");
//                }
//                if (discoveredDevices.containsKey(317)){
//                    System.out.println("存在317的值");
//                    for (int i =0;i<=959;i++){
//                        Map.Entry entry = readObjectProperties(317, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("317读取完毕");
//                }
//                if (discoveredDevices.containsKey(318)){
//                    System.out.println("存在318的值");
//                    for (int i =0;i<=989;i++){
//                        Map.Entry entry = readObjectProperties(318, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("318读取完毕");
//                }
//                if (discoveredDevices.containsKey(325)){
//                    System.out.println("存在325的值");
//                    for (int i =0;i<=179;i++){
//                        Map.Entry entry = readObjectProperties(325, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("325读取完毕");
//                }
//                if (discoveredDevices.containsKey(322)){
//                    System.out.println("存在322的值");
//                    for (int i =0;i<=989;i++){
//                        Map.Entry entry = readObjectProperties(322, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
//                    System.out.println("322读取完毕");
//                }
//新增散表统计
                //1筒 361 4个点
                if (discoveredDevices.containsKey(361)){
                    System.out.println("存在361的值");
                    for (int i =0;i<=3;i++){
                        Map.Entry entry = readObjectProperties(361, ObjectType.analogInput, i);
                        maps.put(entry.getKey(),entry.getValue());
                    }
                    System.out.println("361读取完毕");
                }
                //2筒 362 7
                if (discoveredDevices.containsKey(362)){
                    System.out.println("存在362的值");
                    for (int i =0;i<=6;i++){
                        Map.Entry entry = readObjectProperties(362, ObjectType.analogInput, i);
                        maps.put(entry.getKey(),entry.getValue());
                    }
                    System.out.println("362读取完毕");
                }
                //3筒 363 10个
                if (discoveredDevices.containsKey(363)){
                    System.out.println("存在363的值");
                    for (int i =0;i<=9;i++){
                        Map.Entry entry = readObjectProperties(363, ObjectType.analogInput, i);
                        maps.put(entry.getKey(),entry.getValue());
                    }
                    System.out.println("363读取完毕");
                }
                //4筒 364 5个
                if (discoveredDevices.containsKey(364)){
                    System.out.println("存在364的值");
                    for (int i =0;i<=4;i++){
                        Map.Entry entry = readObjectProperties(364, ObjectType.analogInput, i);
                        maps.put(entry.getKey(),entry.getValue());
                    }
                    System.out.println("364读取完毕");
                }
                //6筒 366 21
                if (discoveredDevices.containsKey(366)){
                    System.out.println("存在366的值");
                    for (int i =0;i<=20;i++){
                        Map.Entry entry = readObjectProperties(366, ObjectType.analogInput, i);
                        maps.put(entry.getKey(),entry.getValue());
                    }
                    System.out.println("366读取完毕");
                }
                //7筒 367 3
                if (discoveredDevices.containsKey(367)){
                    System.out.println("存在367的值");
                    for (int i =0;i<=2;i++){
                        Map.Entry entry = readObjectProperties(367, ObjectType.analogInput, i);
                        maps.put(entry.getKey(),entry.getValue());
                    }
                    System.out.println("367读取完毕");
                }


                return maps;
            } else {
                System.out.println("发现设备数量不足-");
                return maps;
            }



        } catch (Exception e) {
            e.printStackTrace();
            return maps;
        }
    }

    /**
     * 读取 325 网关转发的电气数据。
     */
    public  Map<Object,Object> Hq325sj(){
        Map<Object, Object> maps = new LinkedHashMap<>();
        try {
            // 1. 检查连接是否已初始化
            if (localDevice == null || !localDevice.isInitialized()) {
                throw new RuntimeException("BACnet连接未初始化，请检查启动日志");
            }

            if (discoveredDevices.isEmpty()) {
                System.out.println("未发现设备，重新执行发现...");
                this.discoverDevices(5); // 重新发现设备（5秒超时）
            }

            // 读取第一个发现的设备属性（示例）
            if (discoveredDevices.size() >= 8) {

                if (discoveredDevices.containsKey(325)){
                    System.out.println("存在325的值");
                    for (int i =0;i<=179;i++){
                        Map.Entry entry = readObjectProperties(325, ObjectType.analogInput, i);
                        maps.put(entry.getKey(),entry.getValue());
                    }
                    System.out.println("325读取完毕");
                }


                return maps;
            } else {
                System.out.println("发现设备数量不足-");
                return maps;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return maps;
        }
    }
    @Async("asyncTaskExecutor")
    /**
     * 读取高压侧网关的测量数据。
     */
    public Map<Object,Object> HqGySj(){
        Map<Object,Object> maps = new LinkedHashMap<>();

        try{
            // 1. 检查连接是否已初始化
            if (localDevice == null || !localDevice.isInitialized()) {
                throw new RuntimeException("BACnet连接未初始化，请检查启动日志");
            }

            if (discoveredDevices.isEmpty()) {
                System.out.println("未发现设备，重新执行发现...");
                this.discoverDevices(5); // 重新发现设备（5秒超时）
            }
            if (discoveredDevices.size() >= 8) {
                for (int i =0;i<= 276;i++){
                    Map.Entry entry = readObjectProperties(300, ObjectType.analogInput, i);
                    maps.put(entry.getKey(),entry.getValue());
                }

                System.out.println("高压设备读取完成");
            }
            return maps;

        }catch (Exception e){
            System.out.println("发现设备数量不足-");
            return maps;
        }
    }

    /**
     * 读取308设备的第205和206个analogInput的属性（线程安全）
     * @return 包含205和206号信息的Map，key为对象名称，value为属性值
     */
    public Map<Object, Object> readDevice308Specific() {
        Map<Object, Object> result = new HashMap<>();
        int targetDeviceId = 301; // 目标设备ID
        int instance205 = 32;    // 第205个信息
        int instance206 = 92;    // 第206个信息

        // 加锁同步：锁定当前实例，确保多线程调用时共享资源操作有序（根据实际线程安全需求决定是否加锁）
        synchronized (this) {
            try {
                // 1. 检查设备是否已发现
                if (!discoveredDevices.containsKey(targetDeviceId)) {
                    throw new RuntimeException("307设备未发现，请先执行设备发现");
                }

                // 2. 检查连接状态
                if (localDevice == null || !localDevice.isInitialized()) {
                    throw new RuntimeException("BACnet连接未初始化");
                }

                // 3. 读取8-8号信息
                Map.Entry entry205 = readObjectProperties(targetDeviceId, ObjectType.analogInput, 32);
                result.put(entry205.getKey(), entry205.getValue());

                // 4. 读取9-7号信息
                Map.Entry entry206 = readObjectProperties(targetDeviceId, ObjectType.analogInput, 92);
                result.put(entry206.getKey(), entry206.getValue());

                System.out.println("301设备特定信息读取完成");
            } catch (Exception e) {
                e.printStackTrace();
                result.put("error", "读取失败：" + e.getMessage());
            }
        }

        return result;
    }


    /**
     * 读取滤波器相关网关点位。
     */
    public Map<Object,Object> lbqduqu(){
        Map<Object,Object> result = new LinkedHashMap<>();
        try {
            // 1. 检查连接是否已初始化
            if (localDevice == null || !localDevice.isInitialized()) {
                throw new RuntimeException("BACnet连接未初始化，请检查启动日志");
            }
            if (discoveredDevices.isEmpty()) {
                System.out.println("未发现设备，重新执行发现...");
                this.discoverDevices(5); // 重新发现设备（5秒超时）
            }
            if(discoveredDevices.containsKey(309)  ){
                System.out.println("设备中存在309的健-----------");
                for (int i =0;i<= 59;i++){
                    Map.Entry entry = readObjectProperties(309, ObjectType.analogInput, i);
//                    System.out.println(entry.getKey());
                    result.put(entry.getKey(),entry.getValue());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 按网关号和设备号列表读取指定设备数据。
     */
    public Map<Object,Object> qianduanduqu(Integer wangguanhao,List<Integer> shebei){
        Map<Object,Object> result = new LinkedHashMap<>();
        try {
            if(discoveredDevices.containsKey(wangguanhao)  ){
//                System.out.println("设备中存在"+wangguanhao+"的健-----------");
                for (int i :shebei){
//                    System.out.println("读取"+wangguanhao+"|||"+i);
                    Map.Entry entry = readObjectProperties(wangguanhao, ObjectType.analogInput, i);
                    result.put(entry.getKey(),entry.getValue());
//                    System.out.println(entry.getKey()+"设备读取"+entry.getValue());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(result+"源头数据");
        return result;
    }

    /**
     *
     * 异步调用方法查询309的值测试
     *
     */
    public void yibuDemo(int i){
        Map.Entry entry = null;
        try {
            if (i ==1){
                entry = readObjectProperties(309, ObjectType.analogInput, 1);
                System.out.println("方法执行一次："+entry.getKey()+"|"+entry.getValue());
            }else {
                for (int j =0;j<= 44;j++){
                    Thread.sleep(1000);
                    entry = readObjectProperties(309, ObjectType.analogInput, j);
                    System.out.println("方法循环执行"+j+"次："+entry.getKey()+"|"+entry.getValue());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    int i =0;

    private static final Map<Integer, List<Integer>> PDS_2_AND_6_DI_INDICES = createPds2And6DiIndices();

    /**
     * 构建二号配与六号配 DI 报警点的网关实例索引配置。
     */
    private static Map<Integer, List<Integer>> createPds2And6DiIndices() {
        Map<Integer, List<Integer>> indices = new LinkedHashMap<>();
        indices.put(312, Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 588, 618, 648, 678, 708, 738, 768, 798, 828, 858));
        indices.put(313, Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 394, 399, 418, 448, 478, 508, 538, 568, 598, 628, 658, 688, 718, 748, 778, 808, 838, 868, 898, 928, 944, 965));
        indices.put(314, Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 274, 293, 323, 339, 358, 388, 418, 448, 478, 494, 499, 518, 548, 581, 611, 641, 671, 701, 731, 761, 791, 821, 851, 881, 911, 941, 957, 964));
        indices.put(315, Arrays.asList(18, 48, 78, 108, 124, 131, 138, 145, 152, 173, 203, 233, 263, 279, 286, 307, 337, 367, 397, 427, 457, 487, 517, 547, 577, 607, 637, 667, 683, 704, 734, 764, 794, 810, 817, 838, 868, 898, 928, 944));
        indices.put(316, Arrays.asList(18, 48, 64, 69, 74, 93, 123, 153, 183, 213, 243, 273, 303, 333, 363, 393, 423, 453, 483, 499, 504, 523, 553, 583, 613, 643, 673, 703, 733, 763, 793, 823, 853, 883, 913, 943));
        indices.put(317, Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 588, 618, 648, 678, 708, 738, 768, 798, 828, 858, 888, 918, 948));
        indices.put(318, Arrays.asList(18, 48, 78, 108, 138, 154, 173, 203, 233, 249, 254, 273, 303, 333, 349, 368, 398, 428, 458, 488, 518, 534, 539, 558, 588, 618, 648, 664, 685, 715, 745, 775, 805, 835, 865, 895, 925, 955));
        indices.put(322, Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 588, 618, 648, 678, 708, 738, 768, 798, 828, 858, 888, 918, 948, 978));
        indices.put(325, Arrays.asList(18, 48, 78, 108, 138, 168));
        return Collections.unmodifiableMap(indices);
    }

    /**
     * 读取二号配与六号配配置中的 DI1 状态并返回报警轮询结果。
     */
    public Map<Object, Object> baojingPds2And6() {
        Map<Object, Object> points = new LinkedHashMap<>();
        synchronized (this) {
            if (localDevice == null || !localDevice.isInitialized()) {
                throw new IllegalStateException("BACnet设备尚未初始化");
            }
            for (Map.Entry<Integer, List<Integer>> gateway : PDS_2_AND_6_DI_INDICES.entrySet()) {
                for (Integer index : gateway.getValue()) {
                    try {
                        Map.Entry point = readObjectProperties(gateway.getKey(), ObjectType.analogInput, index);
                        String pointName = String.valueOf(point.getKey());
                        if (pointName.matches("(?i).+\\.DI1$") && isValidDiValue(point.getValue())) {
                            points.put(point.getKey(), point.getValue());
                        } else {
                            System.err.println("BACnet DI点名与扫描清单不符，跳过: gateway=" + gateway.getKey()
                                    + ", index=" + index + ", name=" + pointName);
                        }
                    } catch (Exception e) {
                        System.err.println("读取2/6号配电室DI失败: gateway=" + gateway.getKey()
                                + ", index=" + index + ", error=" + e.getMessage());
                    }
                }
            }
        }
        System.out.println("2/6号配电室DI1报警轮询读取到点位数=" + points.size());
        return points;
    }

    /**
     * 判断读取结果是否为有效的 DI 二值状态，过滤错误或无效值。
     */
    private boolean isValidDiValue(Object value) {
        if (value == null) return false;
        String text = value.toString().trim();
        return text.equals("0") || text.equals("1") || text.equals("0.0") || text.equals("1.0");
    }

    /**
     * 按网关和实例索引逐点读取 DI 报警点，并跳过读取失败点继续轮询。
     */
    private void readAlarmPoints(Map<Object, Object> target, int gateway, List<Integer> indices) {
        for (Integer index : indices) {
            try {
                Map.Entry point = readObjectProperties(gateway, ObjectType.analogInput, index);
                String pointName = String.valueOf(point.getKey());
                if (pointName.matches("(?i).+\\.DI1$") && isValidDiValue(point.getValue())) {
                    target.put(point.getKey(), point.getValue());
                } else {
                    System.err.println("报警索引不是DI1，跳过: gateway=" + gateway + ", index=" + index
                            + ", name=" + pointName);
                }
            } catch (Exception e) {
                // Some gateway firmware exposes incomplete point tables. Skip only the missing point.
                System.err.println("DI报警点读取失败: gateway=" + gateway + ", index=" + index
                        + ", error=" + e.getMessage());
            }
        }
    }

    /**
     * 读取各配电室配置的 DI 报警点并汇总为状态映射。
     */
    public Map<Object,Object> baojing(){
        Map<Object,Object> maps = new LinkedHashMap<>();
        synchronized (this) {
            try {
                // 2. 检查连接状态
                if (localDevice == null || !localDevice.isInitialized()) {
                    throw new RuntimeException("BACnet连接未初始化");
                }
                // 3. 读取报警信息
                List<Integer> cms301 =Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 474, 506, 536, 566, 596, 626, 656, 686, 716, 746, 776, 806, 836);
                readAlarmPoints(maps, 301, cms301);
                List<Integer> cms302 =Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 444, 476, 506, 536, 566, 596, 612, 619, 626, 647, 677, 707, 737, 767, 797, 827, 857, 887, 917, 947, 963, 970, 977, 984);
                readAlarmPoints(maps, 302, cms302);
                List<Integer> cms303 =Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 588, 618, 634, 655, 671, 678, 699, 715, 722, 729, 750, 783, 813, 829, 850, 866, 873, 880, 887, 894);
                readAlarmPoints(maps, 303, cms303);
                List<Integer> cms304 =Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 588, 618, 648, 678, 708, 738, 768, 798, 828, 858, 888, 918, 948, 978);
                readAlarmPoints(maps, 304, cms304);
                List<Integer> cms305 =Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 588, 618, 634, 641, 662, 692, 722, 738, 759, 789, 819, 835, 842, 849, 856, 877, 907, 937, 953, 960, 967);
                readAlarmPoints(maps, 305, cms305);
                List<Integer> cms306 =Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 588, 618, 648, 678, 708, 738, 768, 798, 828, 858, 888, 918, 948, 978);
                readAlarmPoints(maps, 306, cms306);
                List<Integer> cms308 =Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 588, 618, 648, 678, 708, 738, 768, 784, 791, 812, 842, 872, 902, 932, 962);
                readAlarmPoints(maps, 308, cms308);
                List<Integer> cms307 =Arrays.asList(18, 48, 78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 591, 621, 651, 681, 711, 727, 734, 755, 785, 815, 845, 875, 905, 935, 951);
                readAlarmPoints(maps, 307, cms307);
                List<Integer> cms309 =Arrays.asList(78, 108, 138, 168, 198, 228, 258, 288, 318, 348, 378, 408, 438, 468, 498, 528, 558, 588, 618);
                readAlarmPoints(maps, 309, cms309);
                maps.putAll(baojingPds2And6());
                System.out.println("异步处理完成"+i);
                i++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return maps;
    }

    public static void main(String[] args) {
        BacnetTool tool = new BacnetTool();
        try {
            // 初始化本地设备
            tool.init();
            if (!tool.discoveredDevices.isEmpty()) {
                int firstDeviceId = tool.discoveredDevices.keySet().iterator().next();
                System.out.println("这是读取到的设备id："+firstDeviceId);
                Map<Object, Object> maps = new LinkedHashMap<>();
//                List<ObjectIdentifier> objectList = tool.getObjectList(firstDeviceId);
//                System.out.println(objectList+"这是读取到的设备");

//                HashMap<Object, Object> QJmap = new HashMap();
                List<Integer> cms  = new ArrayList();
//                for (int i =0;i<=300;i++){
//                        Map.Entry entry = tool.readObjectProperties(300, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    System.out.println(i+""+entry.getKey()+"||"+entry.getValue());
////                        if (entry.getKey().toString().contains("DI1")){
////                            cms.add(i);
//////                            System.out.println("301设备:"+i+" Key: " + entry.getKey() + ", Value: " + entry.getValue());
////                        }
//                }
//                System.out.println("301设备:"+cms);
//                cms.clear();

                for (int i =0;i<=380;i++){
                    Map.Entry entry = tool.readObjectProperties(309, ObjectType.analogInput, i);
                    maps.put(entry.getKey(),entry.getValue());
                    if (entry.getKey().toString().contains("DI1")){
                        cms.add(i);
//                        System.out.println("302设备:"+i+" Key: " + entry.getKey() + ", Value: " + entry.getValue());
                    }
                }
//
                System.out.println("302设备:"+cms);
//                cms.clear();
//                for (int i =0;i<=895;i++){
//                    Map.Entry entry = tool.readObjectProperties(303, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                    if (entry.getKey().toString().contains("DI1")){
//                        cms.add(i);
////                        System.out.println("303设备:"+i+" Key: " + entry.getKey() + ", Value: " + entry.getValue());
//                    }
//                }
//                System.out.println("303设备:"+cms);
//                cms.clear();
//                for (int i =0;i<=989;i++){
//                    Map.Entry entry = tool.readObjectProperties(304, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                    if (entry.getKey().toString().contains("DI1")){
//                        cms.add(i);
////                        System.out.println("304设备:"+i+" Key: " + entry.getKey() + ", Value: " + entry.getValue());
//                    }
//                }
//
//                System.out.println("304设备:"+cms);
//                cms.clear();
//                for (int i =0;i<=1015;i++){
//                    Map.Entry entry = tool.readObjectProperties(305, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                    if (entry.getKey().toString().contains("DI1")){
//                        cms.add(i);
////                        System.out.println("305设备:"+i+" Key: " + entry.getKey() + ", Value: " + entry.getValue());
//                    }
//                }
//
//                System.out.println("305设备:"+cms);
//                cms.clear();
//                for (int i =0;i<=898;i++){
//                    Map.Entry entry = tool.readObjectProperties(306, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                    if (entry.getKey().toString().contains("DI1")){
//                        cms.add(i);
////                        System.out.println("306设备:"+i+" Key: " + entry.getKey() + ", Value: " + entry.getValue());
//                    }
//                }
//
//                System.out.println("306设备:"+cms);
//                cms.clear();
//                for (int i =0;i<=1019;i++){
//                    Map.Entry entry = tool.readObjectProperties(308, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
//                    if (entry.getKey().toString().contains("DI1")){
//                        cms.add(i);
////                        System.out.println("308设备:"+i+" Key: " + entry.getKey() + ", Value: " + entry.getValue());
//                    }
//                }
//                System.out.println("308设备:"+cms);
//                cms.clear();
//                for (int i =0;i<=30;i++){
//                    Map.Entry entry = tool.readObjectProperties(307, ObjectType.analogInput, i);
//                    maps.put(entry.getKey(),entry.getValue());
////                    if (entry.getKey().toString().contains("DI1")){
////                        cms.add(i);
////                   }
//                    System.out.println("307设备:"+i+" Key: " + entry.getKey() + ", Value: " + entry.getValue());
//
//                }
//                System.out.println("307设备:"+cms);
//                cms.clear();

//                 输出存储的键值对-
//                for (Map.Entry<Object, Object> entry : maps.entrySet()) {
////                    if (entry.getKey().toString().equals("M3.Ia")){
//                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
////                    }
//                }
//                for (int j =1;j<1000;j++) {
//                    // 读取第一个对象的属性（示例：读取第一个 Analog Input） //13-7ADSQ
//                    for (int i =1;i<= 30;i++){
//                        Map.Entry entry = tool.readObjectProperties(firstDeviceId, ObjectType.analogInput, i);
//                        maps.put(entry.getKey(),entry.getValue());
//                    }
////                System.out.println(maps);
//                    // 输出存储的键值对
//                    if (QJmap.isEmpty()){
//                        QJmap.putAll(maps);
//                    }else {
//                        String key,value;
//                        ArrayList<Object> list = new ArrayList<>();
//                        for (Map.Entry<Object, Object> entry : maps.entrySet()) {
//                            key = entry.getKey().toString();  //4AA8.DI1
//                            value = entry.getValue().toString(); //1
//
////                            System.out.println(key+"新设备信息："+value);
//                            if (QJmap.containsKey(entry.getKey())){
//                                String oldvalue = QJmap.get(entry.getKey()).toString();
////                                System.out.println(key+"老设备信息："+oldvalue);
//                                if (!oldvalue.equals(value)){
//                                    System.out.println("设备跳闸"+key+"信息："+value+"|"+oldvalue );
//                                    list.add(key);
//                                    //重置信息
//                                    QJmap.putAll(maps);
//                                }
//                            }
//                        }
//                    }
//
////                    for (Map.Entry<Object, Object> entry : maps.entrySet()) {
////                        System.out.println("这是读取的第"+j+"遍Key: " + entry.getKey() + ", Value: " + entry.getValue());
////                    }
//
//                    System.out.println("---------------第"+j+"次完成分割线-------------------");
//                    Thread.sleep(3000);
//                }
            } else {
                System.out.println("未发现任何设备");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage()+"报错了");
        } finally {
            tool.shutdown();
        }
    }

    /**
     * 读取变压器相关测量点位。
     */
    public Map<Object, Object> byqduqu() {

        Map<Object,Object> result = new LinkedHashMap<>();
        try {
            // 1. 检查连接是否已初始化
            if (localDevice == null || !localDevice.isInitialized()) {
                throw new RuntimeException("BACnet连接未初始化，请检查启动日志");
            }
            if (discoveredDevices.isEmpty()) {
                System.out.println("未发现设备，重新执行发现...");
                this.discoverDevices(5); // 重新发现设备（5秒超时）
            }
            if(discoveredDevices.containsKey(300)  ){
                System.out.println("设备中存在300的健-----------");
                for (int i =0;i<= 44;i++){
                    Map.Entry entry = readObjectProperties(309, ObjectType.analogInput, i);
//                    System.out.println(entry.getKey());
                    result.put(entry.getKey(),entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
