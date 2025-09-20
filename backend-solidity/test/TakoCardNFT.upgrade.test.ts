import { ethers, upgrades } from "hardhat";
import { expect } from "chai";
import { HardhatEthersSigner } from "@nomicfoundation/hardhat-ethers/signers";
import { TakoCardNFT, TakoCardNFT_V2 } from "../typechain-types";

describe("TakoCardNFT Upgrades", function () {
  let owner: HardhatEthersSigner;
  let backendAdmin: HardhatEthersSigner;
  let user1: HardhatEthersSigner;
  let takoCardNFT_V1: TakoCardNFT; // V1 컨트랙트 인스턴스

  beforeEach(async function () {
    [owner, backendAdmin, user1] = await ethers.getSigners();
    
    const TakoCardNFT_V1_Factory = await ethers.getContractFactory("TakoCardNFT");

    // deployProxy를 사용하여 프록시 컨트랙트를 배포합니다.
    // .deploy()가 아닌 upgrades.deployProxy()를 사용하는 것이 핵심입니다.
    takoCardNFT_V1 = (await upgrades.deployProxy(
      TakoCardNFT_V1_Factory,
      [owner.address], // initialize 함수에 전달할 인자
      { initializer: 'initialize', kind: 'uups' }
    )) as unknown as TakoCardNFT;
    
    await takoCardNFT_V1.waitForDeployment();
    // backendAdmin 설정
    await takoCardNFT_V1.connect(owner).setBackendAdmin(backendAdmin.address);
  });

  it("초기화 함수는 두 번 호출할 수 없어야 합니다.", async function () {
    // 이미 배포 시 초기화가 되었으므로, 다시 호출하면 실패해야 합니다.
    await expect(
      takoCardNFT_V1.initialize(owner.address)
    ).to.be.revertedWithCustomError(takoCardNFT_V1, "InvalidInitialization");
  });
  
  it("V2로 성공적으로 업그레이드하고, 새로운 함수를 호출할 수 있어야 합니다.", async function () {
    const TakoCardNFT_V2_Factory = await ethers.getContractFactory("TakoCardNFT_V2");
    
    const proxyAddress = await takoCardNFT_V1.getAddress();

    // upgradeProxy를 사용하여 기존 프록시의 로직을 V2로 교체합니다.
    const upgraded = await upgrades.upgradeProxy(proxyAddress, TakoCardNFT_V2_Factory) as unknown as TakoCardNFT_V2;

    // V2에만 존재하는 version() 함수를 호출해봅니다.
    expect(await upgraded.version()).to.equal("V2");
  });

  it("업그레이드 후에도 기존 데이터(상태)가 보존되어야 합니다.", async function () {
    const tokenId = 1;
    // 1. V1 상태에서 NFT를 발행하여 데이터를 만듭니다.
    await takoCardNFT_V1.connect(backendAdmin).safeMint(user1.address, tokenId);
    expect(await takoCardNFT_V1.ownerOf(tokenId)).to.equal(user1.address);
    
    const proxyAddress = await takoCardNFT_V1.getAddress();
    
    // 2. V2로 업그레이드합니다.
    const TakoCardNFT_V2_Factory = await ethers.getContractFactory("TakoCardNFT_V2");
    const upgraded = await upgrades.upgradeProxy(proxyAddress, TakoCardNFT_V2_Factory) as unknown as TakoCardNFT_V2;
    
    // 3. V2 상태에서 V1 때 만들었던 데이터가 그대로 있는지 확인합니다.
    expect(await upgraded.ownerOf(tokenId)).to.equal(user1.address);
  });
});