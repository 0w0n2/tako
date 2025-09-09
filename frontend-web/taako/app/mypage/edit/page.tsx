import Image from 'next/image';

export default function EditPage() {
  return (
    <div>
      <h2>내 정보 수정</h2>
      <div>
        <form className="flex flex-col gap-10 my-10">
          <div>
            <p className="mb-4">이메일</p>
            <div className='flex flex-col gap-3'>
              <div className="w-[350px] px-5 py-3 text-[#a5a5a5] bg-[#383838] rounded-lg border-1 border-[#353535] text-sm">
                doriconi@gmail.com
              </div>
            </div>
          </div>

          <div>
            <p className="mb-4">닉네임</p>
            <div className="flex flex-col gap-3">
              <div className='flex gap-4'>
                <input
                  className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm"
                  type="text"
                  placeholder="닉네임을 입력해주세요"/>
                <button className="min-w-[140px] px-8 bg-[#3A468C] rounded-lg text-md">중복체크</button>
              </div>
              { false && (
                <div className="text-[#FF3737] text-md mt-1 flex gap-2 items-center">
                  <Image src="/icon/error.svg" width={18} height={18} alt="error" />
                  이미 존재하는 닉네임입니다.
                </div>
              ) || (
                <div className="text-[#40C057] text-md mt-1 flex gap-2 items-center">
                  <Image src="/icon/correct.svg" width={18} height={18} alt="error" />
                  사용 가능한 닉네임입니다.
                </div>
              )}
            </div>
          </div>
          <button
            type="submit"
            className='w-[150px] px-8 py-3 cursor-pointer rounded-lg'
            style={{
              background: 'linear-gradient(137deg, #4557BF 20%, #3A468C 100%)'
            }}
          >
            정보수정
          </button>
        </form>
      </div>
    </div>
  );
}
