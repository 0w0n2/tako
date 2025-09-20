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
import org.web3j.abi.datatypes.CustomError;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint8;
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
public class AuctionEscrow extends Contract {
    public static final String BINARY = "// SPDX-License-Identifier: UNLICENSED\r\n"
            + "pragma solidity ^0.8.28;\r\n"
            + "\r\n"
            + "import \"@openzeppelin/contracts-upgradeable/proxy/utils/Initializable.sol\";\r\n"
            + "import \"@openzeppelin/contracts-upgradeable/utils/ReentrancyGuardUpgradeable.sol\";\r\n"
            + "\r\n"
            + "// TakoCardNFT의 함수를 호출하기 위한 인터페이스 정의\r\n"
            + "interface ITakoCardNFT {\r\n"
            + "    function transferFrom(address from, address to, uint256 tokenId) external;\r\n"
            + "    function getApproved(uint256 tokenId) external view returns (address operator);\r\n"
            + "    function isApprovedForAll(address owner, address operator) external view returns (bool);\r\n"
            + "}\r\n"
            + "\r\n"
            + "/**\r\n"
            + "    @title AuctionEscrow (Upgradeable)\r\n"
            + "    @dev 낙찰된 경매 건에 대한 대금을 안전하게 예치(escrow)하고 정산하는 컨트랙트\r\n"
            + "    [동작 플로우]\r\n"
            + "    1. 백엔드 서버(Spring)가 (판매자, 구매자, 낙찰가) 정보를 담아 컨트랙트를 배포\r\n"
            + "    2. 구매자가 낙찰가를 컨트랙트에 입금 (deposit)\r\n"
            + "    3. 구매자가 상품 수령 후 구매 확정 (confirmReceipt)\r\n"
            + "    4. 판매자가 예치된 대금을 인출 (releaseFunds)\r\n"
            + " */\r\n"
            + "contract AuctionEscrow is Initializable, ReentrancyGuardUpgradeable {\r\n"
            + "    // --- 상태 변수, Enum ---\r\n"
            + "    address public seller; // 판매자 주소\r\n"
            + "    address public buyer; // 구매자 주소\r\n"
            + "    uint256 public amount; // 거래액 (낙찰가)\r\n"
            + "\r\n"
            + "    // --- 거래의 진행 상태 ---\r\n"
            + "    enum State {\r\n"
            + "        AwaitingPayment, // 0: 생성 됨 (구매자 입금 대기 중)\r\n"
            + "        AwaitingConfirmation, // 1: 구매자의 수령 확인 대기 중 (입금 후 배송 중)\r\n"
            + "        Complete, // 2: 거래 완료 (판매자에게 대금 전송)\r\n"
            + "        Canceled // 3: 거래 취소 (구매자에게 환불)\r\n"
            + "    }\r\n"
            + "    State public currentState; // 현재 거래 상태\r\n"
            + "\r\n"
            + "    // --- 1. 어떤 NFT를 거래할지 저장할 변수 추가 ---\r\n"
            + "    ITakoCardNFT public takoNFT;\r\n"
            + "    uint256 public tokenId;\r\n"
            + "\r\n"
            + "    // --- 이벤트 ---\r\n"
            + "    event Deposited(address indexed buyer, uint256 amount); // 입금 완료 이벤트\r\n"
            + "    event ReceiptConfirmed(address indexed buyer);\r\n"
            + "    event FundsReleased(address indexed seller, uint256 amount); // 대금 정산 완료 이벤트\r\n"
            + "    event Canceled(address indexed canceller); // 거래 취소 이벤트\r\n"
            + "\r\n"
            + "    // -- 커스텀 에러 --\r\n"
            + "    error NotSeller();\r\n"
            + "    error NotBuyer();\r\n"
            + "    error InvalidState();\r\n"
            + "    error IncorrectAmount();\r\n"
            + "    error TransferFailed();\r\n"
            + "\r\n"
            + "    // --- 제어자 정의 ---\r\n"
            + "    modifier onlyBuyer() {\r\n"
            + "        if (msg.sender != buyer) revert NotBuyer();\r\n"
            + "        _;\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    modifier onlySeller() {\r\n"
            + "        if (msg.sender != seller) revert NotSeller();\r\n"
            + "        _;\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    modifier inState(State _state) {\r\n"
            + "        if (currentState != _state) revert InvalidState();\r\n"
            + "        _;\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    /// @custom:oz-upgrades-unsafe-allow constructor\r\n"
            + "    constructor() {\r\n"
            + "        _disableInitializers();\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    /**\r\n"
            + "        @dev 컨트랙트 생성자. 백엔드 서버가 배포하며 거래 당사자와 금액을 설정.\r\n"
            + "        @param _seller 판매자의 주소(address)\r\n"
            + "        @param _buyer 경매 낙찰자(구매자)의 주소(address)\r\n"
            + "        @param _amount 낙찰 금액\r\n"
            + "     */\r\n"
            + "    function initialize(\r\n"
            + "        address _seller,\r\n"
            + "        address _buyer,\r\n"
            + "        uint256 _amount,\r\n"
            + "        // --- 2. NFT 정보를 생성자에 추가 ---\r\n"
            + "        address _takoNFTAddress,\r\n"
            + "        uint256 _tokenId\r\n"
            + "    ) public initializer {\r\n"
            + "        seller = _seller;\r\n"
            + "        buyer = _buyer;\r\n"
            + "        amount = _amount; // 컨트랙트 배포와 함께 전송된 ETH를 거래액으로 설정\r\n"
            + "        currentState = State.AwaitingPayment; // 초기 상태 : 입금 대기\r\n"
            + "        takoNFT = ITakoCardNFT(_takoNFTAddress);\r\n"
            + "        tokenId = _tokenId;\r\n"
            + "        __ReentrancyGuard_init();\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    /**\r\n"
            + "        @dev [구매자] 대금 입금(결제)\r\n"
            + "     */\r\n"
            + "    function deposit()\r\n"
            + "        external\r\n"
            + "        payable\r\n"
            + "        onlyBuyer\r\n"
            + "        inState(State.AwaitingPayment)\r\n"
            + "    {\r\n"
            + "        if (msg.value != amount) revert IncorrectAmount();\r\n"
            + "\r\n"
            + "        currentState = State.AwaitingConfirmation;\r\n"
            + "        emit Deposited(buyer, msg.value);\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    /**\r\n"
            + "        @dev [구매자] 상품 수령 확인\r\n"
            + "     */\r\n"
            + "    function confirmReceipt()\r\n"
            + "        external\r\n"
            + "        onlyBuyer\r\n"
            + "        inState(State.AwaitingConfirmation)\r\n"
            + "    {\r\n"
            + "        currentState = State.Complete;\r\n"
            + "        emit ReceiptConfirmed(buyer);\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    /**\r\n"
            + "        @dev [판매자] 대금 인출\r\n"
            + "     */\r\n"
            + "    function releaseFunds() external onlySeller inState(State.Complete) nonReentrant {\r\n"
            + "        require(\r\n"
            + "            takoNFT.getApproved(tokenId) == address(this) ||\r\n"
            + "            takoNFT.isApprovedForAll(seller, address(this)),\r\n"
            + "            \"Contract is not approved for NFT transfer\"\r\n"
            + "        );\r\n"
            + "\r\n"
            + "        takoNFT.transferFrom(seller, buyer, tokenId);\r\n"
            + "\r\n"
            + "        emit FundsReleased(seller, amount);\r\n"
            + "        (bool success, ) = seller.call{value: amount}(\"\");\r\n"
            + "        if (!success) revert TransferFailed();\r\n"
            + "    }\r\n"
            + "}\r\n";

    private static String librariesLinkedBinary;

    public static final String FUNC_AMOUNT = "amount";

    public static final String FUNC_BUYER = "buyer";

    public static final String FUNC_CONFIRMRECEIPT = "confirmReceipt";

    public static final String FUNC_CURRENTSTATE = "currentState";

    public static final String FUNC_DEPOSIT = "deposit";

    public static final String FUNC_INITIALIZE = "initialize";

    public static final String FUNC_RELEASEFUNDS = "releaseFunds";

    public static final String FUNC_SELLER = "seller";

    public static final String FUNC_TAKONFT = "takoNFT";

    public static final String FUNC_TOKENID = "tokenId";

    public static final CustomError INCORRECTAMOUNT_ERROR = new CustomError("IncorrectAmount", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError INVALIDINITIALIZATION_ERROR = new CustomError("InvalidInitialization", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError INVALIDSTATE_ERROR = new CustomError("InvalidState", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError NOTBUYER_ERROR = new CustomError("NotBuyer", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError NOTINITIALIZING_ERROR = new CustomError("NotInitializing", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError NOTSELLER_ERROR = new CustomError("NotSeller", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError REENTRANCYGUARDREENTRANTCALL_ERROR = new CustomError("ReentrancyGuardReentrantCall", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError TRANSFERFAILED_ERROR = new CustomError("TransferFailed", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final Event CANCELED_EVENT = new Event("Canceled", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event DEPOSITED_EVENT = new Event("Deposited", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event FUNDSRELEASED_EVENT = new Event("FundsReleased", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event INITIALIZED_EVENT = new Event("Initialized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint64>() {}));
    ;

    public static final Event RECEIPTCONFIRMED_EVENT = new Event("ReceiptConfirmed", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected AuctionEscrow(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected AuctionEscrow(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected AuctionEscrow(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected AuctionEscrow(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<CanceledEventResponse> getCanceledEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CANCELED_EVENT, transactionReceipt);
        ArrayList<CanceledEventResponse> responses = new ArrayList<CanceledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CanceledEventResponse typedResponse = new CanceledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.canceller = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static CanceledEventResponse getCanceledEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CANCELED_EVENT, log);
        CanceledEventResponse typedResponse = new CanceledEventResponse();
        typedResponse.log = log;
        typedResponse.canceller = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<CanceledEventResponse> canceledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCanceledEventFromLog(log));
    }

    public Flowable<CanceledEventResponse> canceledEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CANCELED_EVENT));
        return canceledEventFlowable(filter);
    }

    public static List<DepositedEventResponse> getDepositedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEPOSITED_EVENT, transactionReceipt);
        ArrayList<DepositedEventResponse> responses = new ArrayList<DepositedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DepositedEventResponse typedResponse = new DepositedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DepositedEventResponse getDepositedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEPOSITED_EVENT, log);
        DepositedEventResponse typedResponse = new DepositedEventResponse();
        typedResponse.log = log;
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<DepositedEventResponse> depositedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDepositedEventFromLog(log));
    }

    public Flowable<DepositedEventResponse> depositedEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEPOSITED_EVENT));
        return depositedEventFlowable(filter);
    }

    public static List<FundsReleasedEventResponse> getFundsReleasedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(FUNDSRELEASED_EVENT, transactionReceipt);
        ArrayList<FundsReleasedEventResponse> responses = new ArrayList<FundsReleasedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            FundsReleasedEventResponse typedResponse = new FundsReleasedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static FundsReleasedEventResponse getFundsReleasedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(FUNDSRELEASED_EVENT, log);
        FundsReleasedEventResponse typedResponse = new FundsReleasedEventResponse();
        typedResponse.log = log;
        typedResponse.seller = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<FundsReleasedEventResponse> fundsReleasedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getFundsReleasedEventFromLog(log));
    }

    public Flowable<FundsReleasedEventResponse> fundsReleasedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(FUNDSRELEASED_EVENT));
        return fundsReleasedEventFlowable(filter);
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

    public static List<ReceiptConfirmedEventResponse> getReceiptConfirmedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(RECEIPTCONFIRMED_EVENT, transactionReceipt);
        ArrayList<ReceiptConfirmedEventResponse> responses = new ArrayList<ReceiptConfirmedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ReceiptConfirmedEventResponse typedResponse = new ReceiptConfirmedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ReceiptConfirmedEventResponse getReceiptConfirmedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(RECEIPTCONFIRMED_EVENT, log);
        ReceiptConfirmedEventResponse typedResponse = new ReceiptConfirmedEventResponse();
        typedResponse.log = log;
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ReceiptConfirmedEventResponse> receiptConfirmedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getReceiptConfirmedEventFromLog(log));
    }

    public Flowable<ReceiptConfirmedEventResponse> receiptConfirmedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(RECEIPTCONFIRMED_EVENT));
        return receiptConfirmedEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> amount() {
        final Function function = new Function(FUNC_AMOUNT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> buyer() {
        final Function function = new Function(FUNC_BUYER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> confirmReceipt() {
        final Function function = new Function(
                FUNC_CONFIRMRECEIPT, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> currentState() {
        final Function function = new Function(FUNC_CURRENTSTATE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> deposit(BigInteger weiValue) {
        final Function function = new Function(
                FUNC_DEPOSIT, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> initialize(String _seller, String _buyer,
            BigInteger _amount, String _takoNFTAddress, BigInteger _tokenId) {
        final Function function = new Function(
                FUNC_INITIALIZE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _seller), 
                new org.web3j.abi.datatypes.Address(160, _buyer), 
                new org.web3j.abi.datatypes.generated.Uint256(_amount), 
                new org.web3j.abi.datatypes.Address(160, _takoNFTAddress), 
                new org.web3j.abi.datatypes.generated.Uint256(_tokenId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> releaseFunds() {
        final Function function = new Function(
                FUNC_RELEASEFUNDS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> seller() {
        final Function function = new Function(FUNC_SELLER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> takoNFT() {
        final Function function = new Function(FUNC_TAKONFT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> tokenId() {
        final Function function = new Function(FUNC_TOKENID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    @Deprecated
    public static AuctionEscrow load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new AuctionEscrow(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static AuctionEscrow load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new AuctionEscrow(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static AuctionEscrow load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new AuctionEscrow(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static AuctionEscrow load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new AuctionEscrow(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<AuctionEscrow> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(AuctionEscrow.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<AuctionEscrow> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(AuctionEscrow.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<AuctionEscrow> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(AuctionEscrow.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<AuctionEscrow> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(AuctionEscrow.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
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

    public static class CanceledEventResponse extends BaseEventResponse {
        public String canceller;
    }

    public static class DepositedEventResponse extends BaseEventResponse {
        public String buyer;

        public BigInteger amount;
    }

    public static class FundsReleasedEventResponse extends BaseEventResponse {
        public String seller;

        public BigInteger amount;
    }

    public static class InitializedEventResponse extends BaseEventResponse {
        public BigInteger version;
    }

    public static class ReceiptConfirmedEventResponse extends BaseEventResponse {
        public String buyer;
    }
}
