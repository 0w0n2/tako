package com.bukadong.tcg.global.blockchain.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
//import org.web3j.abi.datatypes.CustomError;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class AuctionFactory extends Contract {
    public static final String BINARY = "// SPDX-License-Identifier: MIT\r\n"
            + "pragma solidity ^0.8.28;\r\n"
            + "\r\n"
            + "import \"@openzeppelin/contracts-upgradeable/proxy/utils/Initializable.sol\";\r\n"
            + "import \"@openzeppelin/contracts/proxy/Clones.sol\";\r\n"
            + "import \"@openzeppelin/contracts-upgradeable/access/OwnableUpgradeable.sol\";\r\n"
            + "import \"./AuctionEscrow.sol\";\r\n"
            + "import \"@openzeppelin/contracts-upgradeable/proxy/utils/UUPSUpgradeable.sol\";\r\n"
            + "\r\n"
            + "contract AuctionFactory is Initializable, OwnableUpgradeable, UUPSUpgradeable {\r\n"
            + "    address public implementation;\r\n"
            + "    address[] public allEscrows;\r\n"
            + "\r\n"
            + "    event EscrowCreated(\r\n"
            + "        address indexed newEscrowAddress,\r\n"
            + "        address indexed seller,\r\n"
            + "        address indexed buyer,\r\n"
            + "        uint256 tokenId\r\n"
            + "    );\r\n"
            + "\r\n"
            + "    /// @custom:oz-upgrades-unsafe-allow constructor\r\n"
            + "    constructor() {\r\n"
            + "        _disableInitializers();\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    function initialize(address _implementation) public initializer {\r\n"
            + "        __Ownable_init(msg.sender); // OwnableUpgradeable v5.0+ 에서는 인자가 없습니다.\r\n"
            + "        __UUPSUpgradeable_init();\r\n"
            + "        implementation = _implementation;\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    // --- 4. _authorizeUpgrade 함수 추가 ---\r\n"
            + "    // 이 컨트랙트의 owner만이 업그레이드를 승인할 수 있도록 설정\r\n"
            + "    function _authorizeUpgrade(address newImplementation) internal override onlyOwner {}\r\n"
            + "\r\n"
            + "    /**\r\n"
            + "     * @dev 새로운 AuctionEscrow 프록시 컨트랙트 생성 (수정된 버전)\r\n"
            + "     */\r\n"
            + "    function createEscrow(\r\n"
            + "        address _seller,\r\n"
            + "        address _buyer,\r\n"
            + "        uint256 _amount,\r\n"
            + "        address _takoNFTAddress, // NFT 컨트랙트 주소 추가\r\n"
            + "        uint256 _tokenId        // NFT 토큰 ID 추가\r\n"
            + "    ) external returns (address) {\r\n"
            + "        // OwnableUpgradeable v5.0+ 에서는 onlyOwner만 호출하도록 제어하는 것이 좋습니다.\r\n"
            + "        // require(msg.sender == owner(), \"Not authorized\");\r\n"
            + "\r\n"
            + "        address newEscrow = Clones.clone(implementation);\r\n"
            + "        // 5개의 인자를 모두 전달하여 initialize 함수 호출\r\n"
            + "        AuctionEscrow(newEscrow).initialize(_seller, _buyer, _amount, _takoNFTAddress, _tokenId);\r\n"
            + "\r\n"
            + "        allEscrows.push(newEscrow);\r\n"
            + "        emit EscrowCreated(newEscrow, _seller, _buyer, _tokenId);\r\n"
            + "\r\n"
            + "        return newEscrow;\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    function setImplementation(address _newImplementation) external onlyOwner {\r\n"
            + "        implementation = _newImplementation;\r\n"
            + "    }\r\n"
            + "}";

    private static String librariesLinkedBinary;

    public static final String FUNC_UPGRADE_INTERFACE_VERSION = "UPGRADE_INTERFACE_VERSION";

    public static final String FUNC_ALLESCROWS = "allEscrows";

    public static final String FUNC_CREATEESCROW = "createEscrow";

    public static final String FUNC_IMPLEMENTATION = "implementation";

    public static final String FUNC_INITIALIZE = "initialize";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PROXIABLEUUID = "proxiableUUID";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SETIMPLEMENTATION = "setImplementation";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_UPGRADETOANDCALL = "upgradeToAndCall";

//    public static final CustomError ADDRESSEMPTYCODE_ERROR = new CustomError("AddressEmptyCode",
//            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
//    ;
//
//    public static final CustomError ERC1967INVALIDIMPLEMENTATION_ERROR = new CustomError("ERC1967InvalidImplementation",
//            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
//    ;
//
//    public static final CustomError ERC1967NONPAYABLE_ERROR = new CustomError("ERC1967NonPayable",
//            Arrays.<TypeReference<?>>asList());
//    ;
//
//    public static final CustomError FAILEDCALL_ERROR = new CustomError("FailedCall",
//            Arrays.<TypeReference<?>>asList());
//    ;
//
//    public static final CustomError FAILEDDEPLOYMENT_ERROR = new CustomError("FailedDeployment",
//            Arrays.<TypeReference<?>>asList());
//    ;
//
//    public static final CustomError INSUFFICIENTBALANCE_ERROR = new CustomError("InsufficientBalance",
//            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
//    ;
//
//    public static final CustomError INVALIDINITIALIZATION_ERROR = new CustomError("InvalidInitialization",
//            Arrays.<TypeReference<?>>asList());
//    ;
//
//    public static final CustomError NOTINITIALIZING_ERROR = new CustomError("NotInitializing",
//            Arrays.<TypeReference<?>>asList());
//    ;
//
//    public static final CustomError OWNABLEINVALIDOWNER_ERROR = new CustomError("OwnableInvalidOwner",
//            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
//    ;
//
//    public static final CustomError OWNABLEUNAUTHORIZEDACCOUNT_ERROR = new CustomError("OwnableUnauthorizedAccount",
//            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
//    ;
//
//    public static final CustomError UUPSUNAUTHORIZEDCALLCONTEXT_ERROR = new CustomError("UUPSUnauthorizedCallContext",
//            Arrays.<TypeReference<?>>asList());
//    ;
//
//    public static final CustomError UUPSUNSUPPORTEDPROXIABLEUUID_ERROR = new CustomError("UUPSUnsupportedProxiableUUID",
//            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
//    ;

    public static final Event ESCROWCREATED_EVENT = new Event("EscrowCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event INITIALIZED_EVENT = new Event("Initialized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint64>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event UPGRADED_EVENT = new Event("Upgraded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected AuctionFactory(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected AuctionFactory(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected AuctionFactory(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected AuctionFactory(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<EscrowCreatedEventResponse> getEscrowCreatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ESCROWCREATED_EVENT, transactionReceipt);
        ArrayList<EscrowCreatedEventResponse> responses = new ArrayList<EscrowCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            EscrowCreatedEventResponse typedResponse = new EscrowCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.newEscrowAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static EscrowCreatedEventResponse getEscrowCreatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ESCROWCREATED_EVENT, log);
        EscrowCreatedEventResponse typedResponse = new EscrowCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.newEscrowAddress = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<EscrowCreatedEventResponse> escrowCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getEscrowCreatedEventFromLog(log));
    }

    public Flowable<EscrowCreatedEventResponse> escrowCreatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ESCROWCREATED_EVENT));
        return escrowCreatedEventFlowable(filter);
    }

    public static List<InitializedEventResponse> getInitializedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(INITIALIZED_EVENT, transactionReceipt);
        ArrayList<InitializedEventResponse> responses = new ArrayList<InitializedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            InitializedEventResponse typedResponse = new InitializedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.version = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static InitializedEventResponse getInitializedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(INITIALIZED_EVENT, log);
        InitializedEventResponse typedResponse = new InitializedEventResponse();
        typedResponse.log = log;
        typedResponse.version = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<InitializedEventResponse> initializedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getInitializedEventFromLog(log));
    }

    public Flowable<InitializedEventResponse> initializedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(INITIALIZED_EVENT));
        return initializedEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public static List<UpgradedEventResponse> getUpgradedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(UPGRADED_EVENT, transactionReceipt);
        ArrayList<UpgradedEventResponse> responses = new ArrayList<UpgradedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            UpgradedEventResponse typedResponse = new UpgradedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.implementation = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static UpgradedEventResponse getUpgradedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(UPGRADED_EVENT, log);
        UpgradedEventResponse typedResponse = new UpgradedEventResponse();
        typedResponse.log = log;
        typedResponse.implementation = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<UpgradedEventResponse> upgradedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getUpgradedEventFromLog(log));
    }

    public Flowable<UpgradedEventResponse> upgradedEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(UPGRADED_EVENT));
        return upgradedEventFlowable(filter);
    }

    public RemoteFunctionCall<String> UPGRADE_INTERFACE_VERSION() {
        final Function function = new Function(FUNC_UPGRADE_INTERFACE_VERSION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> allEscrows(BigInteger param0) {
        final Function function = new Function(FUNC_ALLESCROWS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> createEscrow(String _seller, String _buyer,
            BigInteger _amount, String _takoNFTAddress, BigInteger _tokenId) {
        final Function function = new Function(
                FUNC_CREATEESCROW, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _seller), 
                new org.web3j.abi.datatypes.Address(160, _buyer), 
                new org.web3j.abi.datatypes.generated.Uint256(_amount), 
                new org.web3j.abi.datatypes.Address(160, _takoNFTAddress), 
                new org.web3j.abi.datatypes.generated.Uint256(_tokenId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> implementation() {
        final Function function = new Function(FUNC_IMPLEMENTATION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> initialize(String _implementation) {
        final Function function = new Function(
                FUNC_INITIALIZE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _implementation)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<byte[]> proxiableUUID() {
        final Function function = new Function(FUNC_PROXIABLEUUID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setImplementation(String _newImplementation) {
        final Function function = new Function(
                FUNC_SETIMPLEMENTATION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _newImplementation)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> upgradeToAndCall(String newImplementation,
            byte[] data, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_UPGRADETOANDCALL, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newImplementation), 
                new org.web3j.abi.datatypes.DynamicBytes(data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    @Deprecated
    public static AuctionFactory load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new AuctionFactory(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static AuctionFactory load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new AuctionFactory(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static AuctionFactory load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new AuctionFactory(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static AuctionFactory load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new AuctionFactory(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<AuctionFactory> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(AuctionFactory.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<AuctionFactory> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(AuctionFactory.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<AuctionFactory> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(AuctionFactory.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<AuctionFactory> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(AuctionFactory.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class EscrowCreatedEventResponse extends BaseEventResponse {
        public String newEscrowAddress;

        public String seller;

        public String buyer;

        public BigInteger tokenId;
    }

    public static class InitializedEventResponse extends BaseEventResponse {
        public BigInteger version;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class UpgradedEventResponse extends BaseEventResponse {
        public String implementation;
    }
}
