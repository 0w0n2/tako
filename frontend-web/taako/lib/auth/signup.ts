import api from "@/lib/api";

export const checkEmailDuplicate = async (email: string) => {
  try {
    const res = await api.get("/v1/auth/availability/email", {
      params: { email : email },
    });
    return res.data;
  } catch (error: any) {
    throw new Error(error.response?.data?.message || "중복 확인 실패");
  }
};

// 미 구현
// export const authenticationEmail = async (email:string, verificationType:string) => {
//     try {
//         const res = await api.post("/v1/auth/email/verification", {
//             email,
//             verificationType,
//         })
//         return res.data;
//     }catch(error:any){
//         console.log(error)
//     }
// }

export const checkNicknameDuplicate = async (nickname:string) => {
    try{
        const res = await api.get("/v1/auth/availability/nickname", {
            params: { nickname : nickname }
        });
        return res.data;
    }catch(error:any){
        console.log(error.message)
    }
}

export const signup = async(email:string, password:string, nickname:string, isSocial:boolean, providerName:string) => {
    try{
        const res = await api.post("/v1/auth/sign-up", {
            email,
            password,
            nickname,
            isSocial,
            providerName,
        })
        return res.data;
    }catch(error:any){
        console.log(error.message)
    }
}