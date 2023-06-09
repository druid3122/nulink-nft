package com.project.nulinknft.contract.event.listener.impl;

import com.project.nulinknft.config.ContractsConfig;
import com.project.nulinknft.contract.event.listener.consumer.BlindBoxEventHandler;
import com.project.nulinknft.contract.event.listener.filter.events.ContractsEventEnum;
import com.project.nulinknft.contract.event.listener.filter.events.impl.ContractsEventBuilder;
import com.project.nulinknft.entity.ContractOffset;
import com.project.nulinknft.service.ContractOffsetService;
import com.project.nulinknft.utils.Web3jUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BlockEventListener {

    public static Logger logger = LoggerFactory.getLogger(BlockEventListener.class);

    private final static BigInteger STEP = new BigInteger("500");

    public static final String BLOCK_CONTRACT_FLAG = "BLOCK_CONTRACT_FLAG";

    @Autowired
    ContractsConfig contractsConfig;

    @Autowired
    private Web3jUtils web3jUtils;

    @Autowired
    private ContractOffsetService contractOffsetService;

    @Value("${abey.contracts.start}")
    public String scannerContractStart;

    private Map<String, Event> topicAndContractAddr2EventMap = new HashMap<>();

    // Method format must be: static void functionName(Log)
    //https://www.wangsai.site/2019/12/06/eth-event-topic/
    private Map<String, Method> topicAndContractAddr2CallBackMap = new HashMap<>();


    @Value("${abey.contracts.enabled}")
    private boolean enabled;


    public void start(Integer delayBlocks, Set<String> enablesTaskNames, Set<String> disableTaskNames) throws InterruptedException, NoSuchMethodException {
        if (ObjectUtils.isEmpty(delayBlocks)) {
            delayBlocks = 0;
        }

        if (!enabled) {
            log.info("Delay" + delayBlocks + "_" + "BlockEventListener is disabled! ........");
            return;
        }
        logger.info("Delay" + delayBlocks + "_" + "BlockEventListener start");
        initialize(enablesTaskNames, disableTaskNames);
        blocksEventScanner(delayBlocks);
        logger.info("Delay" + delayBlocks + "_" + "BlockEventListener end");
    }

    private boolean isTaskEnable(Set<String> enablesTaskNames, Set<String> disableTaskNames, String curTaskName) {
        curTaskName = curTaskName.toLowerCase();

        boolean disableTaskNamesIsNull = ObjectUtils.isEmpty(disableTaskNames);
        boolean enablesTaskNamesIsNull = ObjectUtils.isEmpty(enablesTaskNames);

        if (disableTaskNamesIsNull && enablesTaskNamesIsNull) {
            return true;
        }
        else if (!disableTaskNamesIsNull && !enablesTaskNamesIsNull) {
            return true;
        } else if (!disableTaskNamesIsNull && !disableTaskNames.contains(curTaskName)) {
            return true;
        } else if (!enablesTaskNamesIsNull && enablesTaskNames.contains(curTaskName)) {
            return true;
        }


        return false;

    }

    public void initialize(Set<String> enablesTaskNames, Set<String> disableTaskNames) throws NoSuchMethodException {
        if (ObjectUtils.isEmpty(enablesTaskNames)) {
            enablesTaskNames = new HashSet<>();
        } else {
            enablesTaskNames = enablesTaskNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
        }

        if (ObjectUtils.isEmpty(disableTaskNames)) {
            disableTaskNames = new HashSet<>();
        } else {
            disableTaskNames = disableTaskNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
        }


        topicAndContractAddr2EventMap.clear();
        topicAndContractAddr2CallBackMap.clear();


        ContractsConfig.ContractInfo blindBoxCI = contractsConfig.getContractInfo("BlindBox");

        // 如果enablesTaskNames和disableTaskNames中有相同的任务名称， disableTaskNames 优先

        if (isTaskEnable(enablesTaskNames, disableTaskNames, blindBoxCI.getName()) && blindBoxCI.getEnabled()) {
            //blindBox event
            Event eventBuyBlindBox = new ContractsEventBuilder().build(ContractsEventEnum.BUY_BLIND_BOX);
            String topicEventBuyBlindBox = EventEncoder.encode(eventBuyBlindBox).toLowerCase();
            topicAndContractAddr2EventMap.put(topicEventBuyBlindBox + "_" + blindBoxCI.getAddress(), eventBuyBlindBox);
            topicAndContractAddr2CallBackMap.put(topicEventBuyBlindBox + "_" + blindBoxCI.getAddress(), BlindBoxEventHandler.class.getMethod("descBuyBox", Log.class /*,secondParameterTypeClass.class*/));
        }

    }

    public void blocksEventScanner(Integer delayBlocks) throws InterruptedException {

        ContractOffset contractOffset = contractOffsetService.findByContractName("Delay" + delayBlocks + "_" + BLOCK_CONTRACT_FLAG);
        BigInteger start = contractOffset.getBlockOffset();
        if (ObjectUtils.isEmpty(start) || start.compareTo(BigInteger.ZERO) == 0) {
            start = new BigInteger(scannerContractStart);
        }

        logger.info("Delay" + delayBlocks + "_" + "scan all nft albums run() selectMonitorState : " + start);

        BigInteger now = web3jUtils.getBlockNumber(delayBlocks);

        if (start.compareTo(now) >= 0) {
            logger.info("Delay" + delayBlocks + "_" + "scan all nft albums run() return start > now: " + start + " > " + now);
            return;
        }

        while (true) {

            logger.info("Delay" + delayBlocks + "_" + "blocksEventScanner run -------------------");
            if (now.compareTo(BigInteger.ZERO) == 0) {
                logger.info("Delay" + delayBlocks + "_" + "scan all nft albums run() return  now is Zero");
                break;
            }

            // start = new BigInteger("10738436"); //for test

            BigInteger end = start.add(STEP).compareTo(now) > 0 ? now : start.add(STEP);

            logger.info("Delay" + delayBlocks + "_" + "blocksEventScanner run block [" + start + "," + end + "] ");


            filterEvents(delayBlocks, start, end);


            start = end;

            updateOffset(delayBlocks, end);

            if (end.compareTo(now) >= 0) {
                logger.info("Delay" + delayBlocks + "_" + "scan all nft albums run() return  end > now: " + end + " > " + now);
                break;
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    private void filterEvents(Integer delayBlocks, BigInteger start, BigInteger end) {


        List<Event> events = new ArrayList<>(topicAndContractAddr2EventMap.values());

        try {
            EthLog ethlog = web3jUtils.getEthLogs(start, end, events, contractsConfig.getEnabledContractAddresses()/*can be null */);
            logger.info("Delay" + delayBlocks + "_" + "filterEvents size: " + ethlog.getLogs().size());
            if (!ObjectUtils.isEmpty(ethlog) && ethlog.getLogs().size() > 0) {
                eventDispatcher(delayBlocks, ethlog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void eventDispatcher(Integer delayBlocks, EthLog logs) {
        for (EthLog.LogResult logResult : logs.getLogs()) {

            Log log = (Log) logResult.get();

            String contractAddress = log.getAddress().toLowerCase(); //合约地址

            // 注意只有getTopics().get(0) 是真正的订阅主题
            String topic = null;
            try {
                topic = log.getTopics().get(0).toLowerCase();
            } catch (Exception e) {
                continue;
            }

            //获取监听事件topic
            String topicAddress = topic + "_" + contractAddress;
            //Event event = topic2EventMap.get(topicAddress);
            Method callBackMethod = topicAndContractAddr2CallBackMap.get(topicAddress);
            if (null == callBackMethod) {
                continue;
            }
            //
            try {
                //https://stackoverflow.com/questions/4480334/how-to-call-a-method-stored-in-a-hashmap-java
                // Method format must be: static void functionName(Log, Album)
                logger.info("Delay" + delayBlocks + "_" + "eventDispatcher call function: {} ", callBackMethod.getName());
                callBackMethod.invoke(null, log);

            } catch (Exception e) {
                logger.info("Delay" + delayBlocks + "_" + "scan all nft albums run() function {} exception: {}", callBackMethod.getName(), e.getMessage());
            }

        }

    }

    private void updateOffset(Integer delayBlocks, BigInteger offset) {

        String contractAddress = "Delay" + delayBlocks + "_" + BLOCK_CONTRACT_FLAG;

        ContractOffset contractOffset = contractOffsetService.findByContractName(contractAddress);
        if (null == contractOffset) {
            contractOffset.setBlockOffset(offset);
            contractOffsetService.update(contractOffset);
        }
    }

}
