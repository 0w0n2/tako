# Solidity 기본 문법
## ✨ Contract와 Function

```json
// SPDX-License-Identifier: GPL-3.0 // 소스코드의 라이선스

pragma solidity >=0.8.2 <0.9.0; // 솔리디티 버전

/**
 * @title Storage
 * @dev Store & retrieve value in a variable
 * @custom:dev-run-script ./scripts/deploy_with_ethers.ts
 */
contract Storage { // 컨트랙트의 범위 : {} 중괄호 내
    uint256 number; // 상태변수(State Variable) / 블록체인에 값이 저장되는 변수
    // 접근 제어자 지정 가능(external, public, private)

    /**
     * @dev Store value in variable
     * @param num value to store
     */
     // contract 내부에 function을 작성한다.
     // function을 통해서 state variable 을 제어
    function store(uint256 num) public { // save 
        number = num;
    }

    /**
     * @dev Return value 
     * @return value of 'number'
     */
    function retrieve() public view returns (uint256){ // print
        return number;
    }
}
```

## ✨ 자료형
### 기본형 Primitives
- 논리형 : 
    - bool : true / false
- 정수형 :
    - unit : unsigned integer → `uint256`과 동일
    - int : signed integer  
    ※ 8~256 bit 를 표현 가능  
- 주소형 :
    - address : 이더리움의 주소를 표현   
    ※ 0x~ 식으로 적혀있으면 이더리움의 주소를 표현하는 값임    
- 바이트형 : 
    bytes#, byte[] : 데이터를 바이트로 표현

### 접근 제어자 Visibility

| | **`private`** | **`internal`** | **`public`** | **`external`** |
| :--- | :---: | :---: | :---: | :---: |
| **상태 변수 (State Variables)** | O | O | O | X |
| **함수 (Functions)** | O | O | O | O |
