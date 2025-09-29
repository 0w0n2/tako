package com.bukadong.tcg.global.blockchain.util;

import com.bukadong.tcg.global.blockchain.contracts.AuctionEscrow;
import com.bukadong.tcg.global.blockchain.contracts.AuctionFactory;
import com.bukadong.tcg.global.blockchain.contracts.TakoCardNFT;
import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

/**
 * 스마트 컨트랙트 로드 로직
 */
@Component
@RequiredArgsConstructor
public class ContractLoader {

    private final Web3j web3j;
    private final BlockChainProperties blockChainProperties;

    public TakoCardNFT loadTakoCardNft() {
        Credentials credentials = getCredentials();
        String contractAddress = blockChainProperties.contractAddress().takoCardNft();
        ContractGasProvider gasProvider = new DefaultGasProvider();

        return TakoCardNFT.load(
                contractAddress,
                web3j,
                credentials,
                gasProvider
        );
    }

    public AuctionFactory loadAuctionFactory() {
        Credentials credentials = getCredentials();
        String contractAddress = blockChainProperties.contractAddress().auction().factory();
        ContractGasProvider gasProvider = new DefaultGasProvider();

        return AuctionFactory.load(
                contractAddress,
                web3j,
                credentials,
                gasProvider
        );
    }

    public AuctionEscrow loadAuctionEscrow(String contractAddress) {
        Credentials credentials = getCredentials();
        ContractGasProvider gasProvider = new DefaultGasProvider();

        return AuctionEscrow.load(
                contractAddress,
                web3j,
                credentials,
                gasProvider
        );
    }

    private Credentials getCredentials() {
        String privateKey = blockChainProperties.sepolia().privateKey();
        return Credentials.create(privateKey);
    }
}
