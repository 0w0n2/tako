package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionBlockChainService {

    private final Web3j web3j;
    private final BlockChainProperties blockChainProperties;

    /**
     * AuctionFactory 컨트랙트의 createEscrow 함수를 호출하여 새로운 에스크로를 생성
     *
     * @param sellerAddress 판매자 EVM 지갑 주소
     * @param buyerAddress  구매자 EVM 지갑 주소
     * @param amount        거래 금액 (wei 단위)
     * @return 신규 생성된 AuctionEscrow 컨트랙트 주소
     */
    public String createEscrowContract(String sellerAddress, String buyerAddress, BigInteger amount) {
        try {
            String privateKey = blockChainProperties.sepolia().privateKey();
            Credentials credentials = Credentials.create(privateKey);

            String factoryAddress = blockChainProperties.contractAddress().auction().factory();

            AuctionF
        }
    }
}
