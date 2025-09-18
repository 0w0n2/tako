import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

const TakoCardNFTModule = buildModule("TakoCardNFTModule", (m) => {
  // 1. 배포 시점에 동적으로 owner 주소를 받기 위한 파라미터를 정의합니다.
  // 이 파라미터가 없으면, Ignition은 기본적으로 첫 번째 계정(m.getAccount(0))을 사용합니다.
  const initialOwner = m.getParameter("initialOwner", m.getAccount(0));

  // 2. "TakoCardNFT" 컨트랙트를 배포하라고 선언합니다.
  // 두 번째 인자 배열([])에는 생성자(constructor)에 전달할 값을 순서대로 넣습니다.
  // TakoCardNFT의 생성자는 initialOwner 주소를 필요로 하므로, 위에서 정의한 파라미터를 전달합니다.
  const takoCardNFT = m.contract("TakoCardNFT", [initialOwner]);

  // 3. 배포가 완료된 후, takoCardNFT 객체를 반환하도록 설정합니다.
  // 이렇게 하면 배포 결과(주소 등)를 외부에서 쉽게 확인할 수 있습니다.
  return { takoCardNFT };
});

export default TakoCardNFTModule;