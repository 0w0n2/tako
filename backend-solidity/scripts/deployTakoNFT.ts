import hre from "hardhat";

async function main() {
  // --- 1. 배포자 정보 확인 ---
  const [deployer] = await hre.ethers.getSigners();
  const address = deployer.address;
  const balance = await hre.ethers.provider.getBalance(address);
  const formattedBalance = hre.ethers.formatEther(balance);
  
  console.log(`✅ 배포를 진행하는 계정: ${address}`);
  console.log(`💰 해당 계정의 잔액: ${formattedBalance} ETH`);
  console.log("----------------------------------------------------");

  // --- 2. TakoCardNFT 컨트랙트 배포 ---
  console.log("TakoCardNFT 컨트랙트 배포를 시작합니다...");

  const TakoCardNFTFactory = await hre.ethers.getContractFactory("TakoCardNFT");
  
  // 생성자(constructor)에 deployer.address를 넘겨 
  // 초기 owner이자 backendAdmin으로 설정합니다.
  const takoCardNFT = await TakoCardNFTFactory.deploy(address);

  await takoCardNFT.waitForDeployment();

  const contractAddress = await takoCardNFT.getAddress();
  console.log(`🚀 TakoCardNFT 컨트랙트가 다음 주소에 성공적으로 배포되었습니다:`);
  console.log(contractAddress);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});