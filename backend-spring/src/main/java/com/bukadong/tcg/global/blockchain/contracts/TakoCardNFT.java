package com.bukadong.tcg.global.blockchain.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.CustomError;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
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
public class TakoCardNFT extends Contract {
    public static final String BINARY = "// SPDX-License-Identifier: MIT\r\n"
            + "// NFT 발행 후, 각 NFT의 경매 이력을 블록체인에 영구적으로 기록\r\n"
            + "// 백엔드 서버가 관리자 역할 수행 발행 및 이력 기록 통제하는 중앙 관리형 구조\r\n"
            + "pragma solidity ^0.8.28;\r\n"
            + "\r\n"
            + "import \"@openzeppelin/contracts-upgradeable/access/OwnableUpgradeable.sol\";\r\n"
            + "import \"@openzeppelin/contracts-upgradeable/token/ERC721/ERC721Upgradeable.sol\";\r\n"
            + "import \"@openzeppelin/contracts-upgradeable/proxy/utils/Initializable.sol\";\r\n"
            + "import \"@openzeppelin/contracts-upgradeable/proxy/utils/UUPSUpgradeable.sol\";\r\n"
            + "\r\n"
            + "contract TakoCardNFT is Initializable, ERC721Upgradeable, OwnableUpgradeable, UUPSUpgradeable {\r\n"
            + "    struct AuctionHistory {\r\n"
            + "        address seller;         // 경매 개시자 지갑 주소\r\n"
            + "        address buyer;          // 입찰자 지갑 주소\r\n"
            + "        uint256 price;\r\n"
            + "        uint256 gradeId;\r\n"
            + "        uint256 timestamp;\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    // 핵심 데이터 구조: tokenId별 경매 이력 배열\r\n"
            + "    mapping(uint256 => AuctionHistory[]) private auctionHistories;\r\n"
            + "    // 백엔드 서버의 지갑주소\r\n"
            + "    address private backendAdmin;\r\n"
            + "\r\n"
            + "    // --- 클레임 기능 추가 1 : 새로운 데이터 저장 공간 ---\r\n"
            + "    // 토큰 ID별 시크릿 코드의 해시값을 저장 (보안을 위해 원본이 아닌 해시값 저장)\r\n"
            + "    mapping(uint256 => bytes32) private tokenSecrets;\r\n"
            + "    // 한번 사용된 시크릿 코드를 기록하여 재사용 방지\r\n"
            + "    mapping(bytes32 => bool) private usedSecrets;\r\n"
            + "\r\n"
            + "    /// @custom:oz-upgrades-unsafe-allow constructor\r\n"
            + "       constructor() {\r\n"
            + "        _disableInitializers();\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    function initialize(address initialOwner) public initializer {\r\n"
            + "        // 상속받은 컨트랙트들의 초기화 함수를 호출\r\n"
            + "        __ERC721_init(\"TakoNFT\", \"TAKO_RECORD\");\r\n"
            + "        __Ownable_init(initialOwner);\r\n"
            + "        __UUPSUpgradeable_init();\r\n"
            + "\r\n"
            + "        // 기존 constructor의 로직을 그대로 가져옴\r\n"
            + "        backendAdmin = initialOwner;\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    function _authorizeUpgrade(address newImplementation) internal override onlyOwner {}\r\n"
            + "\r\n"
            + "    // backendAdmin만 접근 가능\r\n"
            + "    modifier onlyBackendAdmin() {\r\n"
            + "        require(msg.sender == backendAdmin, \"Not authorized: backend only\");\r\n"
            + "        _;\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    // 관리자만 호출 가능한 NFT 발행 함수\r\n"
            + "    function safeMint(address to, uint256 tokenId) external onlyBackendAdmin {\r\n"
            + "        require(to == backendAdmin, \"Can only mint to backend admin\");\r\n"
            + "        _safeMint(to, tokenId);\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    // --- 클레임 기능 추가 2: 백엔드용 시크릿 등록 함수 ---\r\n"
            + "    /**\r\n"
            + "     * @dev 백엔드 서버가 NFT 발행 후, 해당 NFT를 클레임할 수 있는 시크릿 코드의 해시를 등록합니다.\r\n"
            + "     * @param tokenId 시크릿을 등록할 NFT의 ID\r\n"
            + "     * @param secretHash keccak256으로 해시된 시크릿 코드\r\n"
            + "     */\r\n"
            + "    function registerSecret(uint256 tokenId, bytes32 secretHash) external onlyBackendAdmin {\r\n"
            + "        require(ownerOf(tokenId) == backendAdmin, \"NFT is not owned by admin\");\r\n"
            + "        tokenSecrets[tokenId] = secretHash;\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    // --- 클레임 기능 추가 3: 사용자용 클레임 함수 ---\r\n"
            + "    /**\r\n"
            + "     * @dev 사용자가 시크릿 코드를 사용하여 NFT의 소유권을 직접 가져갑니다(claim).\r\n"
            + "     * @param tokenId 클레임할 NFT의 ID\r\n"
            + "     * @param secret 사용자가 실물 카드에서 확인한 원본 시크릿 코드 (문자열)\r\n"
            + "     */\r\n"
            + "    function claim(uint256 tokenId, string calldata secret) external {\r\n"
            + "        // 1. NFT가 클레임 가능한 상태인지 확인 (소유주가 admin인지)\r\n"
            + "        require(ownerOf(tokenId) == backendAdmin, \"NFT already claimed or not owned by admin\");\r\n"
            + "\r\n"
            + "        // 2. 사용자가 제출한 시크릿 코드가 유효한지 확인\r\n"
            + "        bytes32 secretHash = keccak256(abi.encodePacked(secret));\r\n"
            + "        require(tokenSecrets[tokenId] == secretHash, \"Invalid secret code\");\r\n"
            + "        require(!usedSecrets[secretHash], \"Secret code already used\");\r\n"
            + "\r\n"
            + "        // 3. 시크릿 코드를 '사용 완료'로 기록 (재사용 방지)\r\n"
            + "        usedSecrets[secretHash] = true;\r\n"
            + "\r\n"
            + "        // 4. NFT 소유권을 함수 호출자(사용자)에게 이전\r\n"
            + "        _transfer(backendAdmin, msg.sender, tokenId);\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    // 관리자만 호출 가능한 경매 이력 추가 함수\r\n"
            + "    function addAuctionHistory(\r\n"
            + "        uint256 tokenId,\r\n"
            + "        address seller,\r\n"
            + "        address buyer,\r\n"
            + "        uint256 price,\r\n"
            + "        uint256 gradeId\r\n"
            + "    ) external onlyBackendAdmin {\r\n"
            + "        auctionHistories[tokenId].push(AuctionHistory({\r\n"
            + "            seller: seller,\r\n"
            + "            buyer: buyer,\r\n"
            + "            price: price,\r\n"
            + "            gradeId: gradeId,\r\n"
            + "            timestamp: block.timestamp\r\n"
            + "        }));\r\n"
            + "    }\r\n"
            + "\r\n"
            + "    // cardId별 경매 이력 조회 함수 (읽기용, 누구나 호출 가능)\r\n"
            + "    // TODO : cardId가 아닌 tokenId로 변경 필요\r\n"
            + "    function getAuctionHistories(uint256 cardId) external view returns (AuctionHistory[] memory) {\r\n"
            + "        return auctionHistories[cardId];\r\n"
            + "    }\r\n"
            + "    \r\n"
            + "    // 필요한 경우 backendAdmin 주소 변경 함수 (소유자만 가능)\r\n"
            + "    function setBackendAdmin(address newAdmin) external onlyOwner {\r\n"
            + "        backendAdmin = newAdmin;\r\n"
            + "    }\r\n"
            + "}";

    private static String librariesLinkedBinary;

    public static final String FUNC_UPGRADE_INTERFACE_VERSION = "UPGRADE_INTERFACE_VERSION";

    public static final String FUNC_ADDAUCTIONHISTORY = "addAuctionHistory";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_CLAIM = "claim";

    public static final String FUNC_GETAPPROVED = "getApproved";

    public static final String FUNC_GETAUCTIONHISTORIES = "getAuctionHistories";

    public static final String FUNC_INITIALIZE = "initialize";

    public static final String FUNC_ISAPPROVEDFORALL = "isApprovedForAll";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_OWNEROF = "ownerOf";

    public static final String FUNC_PROXIABLEUUID = "proxiableUUID";

    public static final String FUNC_REGISTERSECRET = "registerSecret";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SAFEMINT = "safeMint";

    public static final String FUNC_safeTransferFrom = "safeTransferFrom";

    public static final String FUNC_SETAPPROVALFORALL = "setApprovalForAll";

    public static final String FUNC_SETBACKENDADMIN = "setBackendAdmin";

    public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOKENURI = "tokenURI";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_UPGRADETOANDCALL = "upgradeToAndCall";

    public static final CustomError ADDRESSEMPTYCODE_ERROR = new CustomError("AddressEmptyCode", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError ERC1967INVALIDIMPLEMENTATION_ERROR = new CustomError("ERC1967InvalidImplementation", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError ERC1967NONPAYABLE_ERROR = new CustomError("ERC1967NonPayable", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError ERC721INCORRECTOWNER_ERROR = new CustomError("ERC721IncorrectOwner", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
    ;

    public static final CustomError ERC721INSUFFICIENTAPPROVAL_ERROR = new CustomError("ERC721InsufficientApproval", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final CustomError ERC721INVALIDAPPROVER_ERROR = new CustomError("ERC721InvalidApprover", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError ERC721INVALIDOPERATOR_ERROR = new CustomError("ERC721InvalidOperator", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError ERC721INVALIDOWNER_ERROR = new CustomError("ERC721InvalidOwner", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError ERC721INVALIDRECEIVER_ERROR = new CustomError("ERC721InvalidReceiver", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError ERC721INVALIDSENDER_ERROR = new CustomError("ERC721InvalidSender", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError ERC721NONEXISTENTTOKEN_ERROR = new CustomError("ERC721NonexistentToken", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final CustomError FAILEDCALL_ERROR = new CustomError("FailedCall", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError INVALIDINITIALIZATION_ERROR = new CustomError("InvalidInitialization", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError NOTINITIALIZING_ERROR = new CustomError("NotInitializing", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError OWNABLEINVALIDOWNER_ERROR = new CustomError("OwnableInvalidOwner", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError OWNABLEUNAUTHORIZEDACCOUNT_ERROR = new CustomError("OwnableUnauthorizedAccount", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError UUPSUNAUTHORIZEDCALLCONTEXT_ERROR = new CustomError("UUPSUnauthorizedCallContext", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError UUPSUNSUPPORTEDPROXIABLEUUID_ERROR = new CustomError("UUPSUnsupportedProxiableUUID", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    ;

    public static final Event APPROVAL_EVENT = new Event("Approval", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
    ;

    public static final Event APPROVALFORALL_EVENT = new Event("ApprovalForAll", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Bool>() {}));
    ;

    public static final Event INITIALIZED_EVENT = new Event("Initialized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint64>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event TRANSFER_EVENT = new Event("Transfer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
    ;

    public static final Event UPGRADED_EVENT = new Event("Upgraded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected TakoCardNFT(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TakoCardNFT(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected TakoCardNFT(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected TakoCardNFT(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<ApprovalEventResponse> getApprovalEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ApprovalEventResponse getApprovalEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(APPROVAL_EVENT, log);
        ApprovalEventResponse typedResponse = new ApprovalEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getApprovalEventFromLog(log));
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

    public static List<ApprovalForAllEventResponse> getApprovalForAllEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVALFORALL_EVENT, transactionReceipt);
        ArrayList<ApprovalForAllEventResponse> responses = new ArrayList<ApprovalForAllEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.operator = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ApprovalForAllEventResponse getApprovalForAllEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(APPROVALFORALL_EVENT, log);
        ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.operator = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getApprovalForAllEventFromLog(log));
    }

    public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVALFORALL_EVENT));
        return approvalForAllEventFlowable(filter);
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

    public static List<TransferEventResponse> getTransferEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TransferEventResponse getTransferEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
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

    public RemoteFunctionCall<TransactionReceipt> addAuctionHistory(BigInteger tokenId,
            String seller, String buyer, BigInteger price, BigInteger gradeId) {
        final Function function = new Function(
                FUNC_ADDAUCTIONHISTORY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId), 
                new org.web3j.abi.datatypes.Address(160, seller), 
                new org.web3j.abi.datatypes.Address(160, buyer), 
                new org.web3j.abi.datatypes.generated.Uint256(price), 
                new org.web3j.abi.datatypes.generated.Uint256(gradeId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> approve(String to, BigInteger tokenId) {
        final Function function = new Function(
                FUNC_APPROVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(tokenId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String owner) {
        final Function function = new Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> claim(BigInteger tokenId, String secret) {
        final Function function = new Function(
                FUNC_CLAIM, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId), 
                new org.web3j.abi.datatypes.Utf8String(secret)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> getApproved(BigInteger tokenId) {
        final Function function = new Function(FUNC_GETAPPROVED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<List> getAuctionHistories(BigInteger cardId) {
        final Function function = new Function(FUNC_GETAUCTIONHISTORIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(cardId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<AuctionHistory>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> initialize(String initialOwner) {
        final Function function = new Function(
                FUNC_INITIALIZE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, initialOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> isApprovedForAll(String owner, String operator) {
        final Function function = new Function(FUNC_ISAPPROVEDFORALL, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner), 
                new org.web3j.abi.datatypes.Address(160, operator)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> name() {
        final Function function = new Function(FUNC_NAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ownerOf(BigInteger tokenId) {
        final Function function = new Function(FUNC_OWNEROF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<byte[]> proxiableUUID() {
        final Function function = new Function(FUNC_PROXIABLEUUID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<TransactionReceipt> registerSecret(BigInteger tokenId,
            byte[] secretHash) {
        final Function function = new Function(
                FUNC_REGISTERSECRET, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId), 
                new org.web3j.abi.datatypes.generated.Bytes32(secretHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> safeMint(String to, BigInteger tokenId) {
        final Function function = new Function(
                FUNC_SAFEMINT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(tokenId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from, String to,
            BigInteger tokenId) {
        final Function function = new Function(
                FUNC_safeTransferFrom, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(tokenId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from, String to,
            BigInteger tokenId, byte[] data) {
        final Function function = new Function(
                FUNC_safeTransferFrom, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(tokenId), 
                new org.web3j.abi.datatypes.DynamicBytes(data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setApprovalForAll(String operator,
            Boolean approved) {
        final Function function = new Function(
                FUNC_SETAPPROVALFORALL, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, operator), 
                new org.web3j.abi.datatypes.Bool(approved)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setBackendAdmin(String newAdmin) {
        final Function function = new Function(
                FUNC_SETBACKENDADMIN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newAdmin)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> supportsInterface(byte[] interfaceId) {
        final Function function = new Function(FUNC_SUPPORTSINTERFACE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes4(interfaceId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> tokenURI(BigInteger tokenId) {
        final Function function = new Function(FUNC_TOKENURI, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferFrom(String from, String to,
            BigInteger tokenId) {
        final Function function = new Function(
                FUNC_TRANSFERFROM, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(tokenId)), 
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
    public static TakoCardNFT load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new TakoCardNFT(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static TakoCardNFT load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TakoCardNFT(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static TakoCardNFT load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new TakoCardNFT(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TakoCardNFT load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new TakoCardNFT(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<TakoCardNFT> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(TakoCardNFT.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<TakoCardNFT> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(TakoCardNFT.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<TakoCardNFT> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TakoCardNFT.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<TakoCardNFT> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TakoCardNFT.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
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

    public static class AuctionHistory extends StaticStruct {
        public String seller;

        public String buyer;

        public BigInteger price;

        public BigInteger gradeId;

        public BigInteger timestamp;

        public AuctionHistory(String seller, String buyer, BigInteger price, BigInteger gradeId,
                BigInteger timestamp) {
            super(new org.web3j.abi.datatypes.Address(160, seller), 
                    new org.web3j.abi.datatypes.Address(160, buyer), 
                    new org.web3j.abi.datatypes.generated.Uint256(price), 
                    new org.web3j.abi.datatypes.generated.Uint256(gradeId), 
                    new org.web3j.abi.datatypes.generated.Uint256(timestamp));
            this.seller = seller;
            this.buyer = buyer;
            this.price = price;
            this.gradeId = gradeId;
            this.timestamp = timestamp;
        }

        public AuctionHistory(Address seller, Address buyer, Uint256 price, Uint256 gradeId,
                Uint256 timestamp) {
            super(seller, buyer, price, gradeId, timestamp);
            this.seller = seller.getValue();
            this.buyer = buyer.getValue();
            this.price = price.getValue();
            this.gradeId = gradeId.getValue();
            this.timestamp = timestamp.getValue();
        }
    }

    public static class ApprovalEventResponse extends BaseEventResponse {
        public String owner;

        public String approved;

        public BigInteger tokenId;
    }

    public static class ApprovalForAllEventResponse extends BaseEventResponse {
        public String owner;

        public String operator;

        public Boolean approved;
    }

    public static class InitializedEventResponse extends BaseEventResponse {
        public BigInteger version;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger tokenId;
    }

    public static class UpgradedEventResponse extends BaseEventResponse {
        public String implementation;
    }
}
