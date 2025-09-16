import hre from "hardhat";

async function main() {
  // .env 파일과 hardhat.config.ts 에 설정된 정보로 deployer(배포자) 정보를 가져옴
  const [deployer] = await hre.ethers.getSigners();

  // deployer의 주소와 잔액을 가져옴 (단위: wei)
  const address = await deployer.address;
  const balance = await hre.ethers.provider.getBalance(address);

  // wei 단위 -> ETH 단위로 변환
  const formattedBalance = hre.ethers.formatEther(balance);
  
  console.log(`✅ Account: ${address}`);
  console.log(`💰 Account Balance: ${formattedBalance} ETH`);
}

main().catch((error) => {
    console.error(error);
    process.exit(1);
  });