'use client';

import { useEffect, useMemo, useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { useDaumPostcodePopup } from 'react-daum-postcode';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { X, Home, CheckCircle2, Pencil, Trash2, Plus } from 'lucide-react';
import { useAddress } from '@/hooks/useAddress';
import type { AddressRequest, AddressDetail } from '@/types/address';

type Props = { open: boolean; onClose: () => void };
type FormMode = 'add' | 'edit';

export default function AddressBookModal({ open, onClose }: Props) {
  const {
    address,
    defaultAddress,
    handlerGetAddress,
    handlerDeleteAddress,
    handlerDefaultAddress,
    handlerAddAddress,
    handlerGetAddressDetail,
    handlerUpdateAddress,
  } = useAddress();

  const [selectedId, setSelectedId] = useState<number | null>(defaultAddress?.id ?? null);
  const [mode, setMode] = useState<FormMode>('add');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loadingDetail, setLoadingDetail] = useState(false);

  const { register, handleSubmit, control, reset, setValue, formState: { errors, isSubmitting } } =
    useForm<AddressRequest>({
      defaultValues: {
        placeName: '',
        name: '',
        phone: '',
        baseAddress: '',
        addressDetail: '',
        zipcode: '',
        setAsDefault: false,
      }
    });

  useEffect(() => {
    if (!open) return;
    handlerGetAddress();
    if (defaultAddress?.id != null) setSelectedId(defaultAddress.id);
  }, [open]);

  useEffect(() => {
    if (defaultAddress?.id != null) setSelectedId(defaultAddress.id);
  }, [defaultAddress?.id]);

  // 다음 주소
  const openPost = useDaumPostcodePopup('https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js');
  const handleComplete = (data: any) => {
    let fullAddress = data.address;
    let extraAddr = '';
    if (data.addressType === 'R') {
      if (data.bname) extraAddr += data.bname;
      if (data.buildingName) extraAddr += extraAddr ? `, ${data.buildingName}` : data.buildingName;
      if (extraAddr) fullAddress += ` (${extraAddr})`;
    }
    setValue('zipcode', (data.zonecode ?? '').trim());
    setValue('baseAddress', (fullAddress ?? '').trim());
    setValue('addressDetail', '');
  };

  const startAdd = () => {
    setMode('add');
    setEditingId(null);
    reset({
      placeName: '',
      name: '',
      phone: '',
      baseAddress: '',
      addressDetail: '',
      zipcode: '',
      setAsDefault: false,
    });
  };

  const startEdit = async (id: number) => {
    setMode('edit');
    setEditingId(id);
    setLoadingDetail(true);
    try {
      const detail: AddressDetail = await handlerGetAddressDetail(id);
      reset({
        placeName: detail.placeName ?? '',
        name: detail.name ?? '',
        phone: detail.phone ?? '',
        baseAddress: detail.baseAddress ?? '',
        addressDetail: detail.addressDetail ?? '',
        zipcode: detail.zipcode ?? '',
        setAsDefault: false, // 수정시 기본설정은 아래 버튼으로
      });
    } finally {
      setLoadingDetail(false);
    }
  };

  const onSubmit = async (data: AddressRequest) => {
    if (mode === 'add') {
      await handlerAddAddress(
        data.placeName?.trim() || '',
        data.name?.trim() || '',
        data.phone?.trim() || '',
        data.baseAddress?.trim() || '',
        data.addressDetail?.trim() || '',
        data.zipcode?.trim() || '',
        data.setAsDefault ?? false
      );
      if (data.setAsDefault) await handlerGetAddress();
      startAdd();
    } else if (mode === 'edit' && editingId != null) {
      await handlerUpdateAddress(editingId, { ...data, setAsDefault: false });
    }
  };

  const canSetDefault = useMemo(
    () => selectedId != null && selectedId !== defaultAddress?.id,
    [selectedId, defaultAddress?.id]
  );

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[80] flex items-center justify-center">
      {/* Overlay */}
      <div className="absolute inset-0 bg-black/60" onClick={onClose} />

      {/* Modal */}
      <div
        className="
          relative w-full max-w-5xl
          rounded-xl border border-[#353535]
          bg-[#191924] text-[#e1e1e1]
          shadow-2xl
          z-[81]
        "
      >
        {/* Header */}
        <div className="sticky top-0 flex items-center justify-between px-5 py-4 border-b border-[#2a2a3b] bg-[#191924] rounded-t-xl">
          <div className="flex items-center gap-2">
            <Home size={18} className="text-[#b5b5b5]" />
            <h3 className="text-base font-semibold">배송지 관리 (선택 · 추가 · 수정 · 삭제)</h3>
          </div>
          <button
            aria-label="close"
            className="p-2 rounded-md hover:bg-[#242433] text-[#b5b5b5]"
            onClick={onClose}
          >
            <X size={18} />
          </button>
        </div>

        {/* Body */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-5">
          {/* Left: List */}
          <div className="border border-[#353535] rounded-lg bg-[#14141e]">
            <div className="flex items-center justify-between px-3 py-2 border-b border-[#2a2a3b]">
              <div className="text-sm text-[#b5b5b5]">주소 목록</div>
              <Button
                size="sm"
                variant="outline"
                onClick={startAdd}
                className="text-xs !rounded-md bg-[#191924] border border-[#444] text-[#dedede] hover:bg-[#242433]"
              >
                <Plus size={14} className="mr-1" /> 새 주소
              </Button>
            </div>

            <div className="max-h-[52vh] overflow-auto p-3 space-y-3">
              {address.length === 0 && (
                <div className="text-sm text-[#9e9eaa]">등록된 배송지가 없습니다. 우측에서 추가해 주세요.</div>
              )}

              {address.map((a) => {
                const checked = selectedId === a.id;
                return (
                  <div
                    key={a.id}
                    className={`
                      border rounded-md p-3 flex items-start gap-3
                      border-[#353535] bg-[#191924]
                      ${checked ? 'ring-1 ring-[#4b5cff]/40' : ''}
                    `}
                  >
                    <input
                      type="radio"
                      name="addr"
                      value={a.id}
                      checked={checked}
                      onChange={() => setSelectedId(a.id)}
                      className="mt-1 h-4 w-4 cursor-pointer accent-[#4b5cff]"
                    />
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <div className="font-medium">{a.placeName || '이름 없는 주소'}</div>
                        {a.default && (
                          <span className="inline-flex items-center gap-1 text-[10px] px-2 py-0.5 rounded-full bg-green-200/15 text-green-300 border border-green-400/20">
                            <CheckCircle2 size={12} /> 기본
                          </span>
                        )}
                      </div>
                      <div className="text-sm text-[#a5a5b5]">
                        {a.baseAddress} ({a.zipcode})
                      </div>

                      <div className="mt-2 flex gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => startEdit(a.id)}
                          className="text-xs !rounded-md bg-[#191924] border border-[#444] text-[#dedede] hover:bg-[#242433]"
                        >
                          <Pencil size={14} className="mr-1" /> 수정
                        </Button>
                        {!a.default && (
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handlerDeleteAddress(a.id)}
                            className="text-xs !rounded-md bg-[#191924] border border-[#444] text-[#dedede] hover:bg-[#2b1f21]"
                          >
                            <Trash2 size={14} className="mr-1" /> 삭제
                          </Button>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="px-3 py-3 border-t border-[#2a2a3b] flex justify-end">
              <Button
                onClick={async () => {
                  if (selectedId == null) return;
                  await handlerDefaultAddress(selectedId);
                  await handlerGetAddress();
                }}
                disabled={!canSetDefault}
                title={!canSetDefault ? '이미 기본 배송지입니다.' : undefined}
                className="text-sm !rounded-md bg-[#4b5cff] hover:bg-[#3a48d4] text-white"
              >
                기본 배송지로 설정
              </Button>
            </div>
          </div>

          {/* Right: Form */}
          <div className="border border-[#353535] rounded-lg bg-[#14141e]">
            <div className="px-3 py-2 border-b border-[#2a2a3b] flex items-center justify-between">
              <div className="text-sm font-medium">
                {mode === 'add' ? '새 주소 추가' : `주소 수정${loadingDetail ? ' (불러오는 중...)' : ''}`}
              </div>
              {mode === 'edit' && (
                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  onClick={startAdd}
                  className="text-xs !rounded-md bg-[#191924] border border-[#444] text-[#dedede] hover:bg-[#242433]"
                >
                  새 주소 추가로 전환
                </Button>
              )}
            </div>

            <form className="p-3 space-y-3" onSubmit={handleSubmit(onSubmit)}>
              <Field label="배송지 별칭">
                <Input
                  placeholder="예: 우리집, 회사"
                  {...register('placeName')}
                  className="bg-[#14141e] border-[#353535] text-[#e1e1e1] placeholder:text-[#6f6f7b] focus-visible:ring-0 focus-visible:ring-offset-0 focus:border-[#4b5cff]"
                />
              </Field>

              <Field label="수령인" error={errors.name?.message}>
                <Input
                  placeholder="수령인의 이름"
                  {...register('name', { required: '이름을 입력하세요' })}
                  className="bg-[#14141e] border-[#353535] text-[#e1e1e1] placeholder:text-[#6f6f7b] focus-visible:ring-0 focus-visible:ring-offset-0 focus:border-[#4b5cff]"
                />
              </Field>

              <Field label="휴대폰 번호" error={errors.phone?.message}>
                <Input
                  placeholder="휴대폰 번호"
                  {...register('phone', { required: '휴대폰 번호를 입력하세요' })}
                  className="bg-[#14141e] border-[#353535] text-[#e1e1e1] placeholder:text-[#6f6f7b] focus-visible:ring-0 focus-visible:ring-offset-0 focus:border-[#4b5cff]"
                />
              </Field>

              <div className="flex flex-col gap-1">
                <Label className="text-sm text-[#b5b5b5]">우편번호</Label>
                <div className="flex gap-2">
                  <Input
                    readOnly
                    placeholder="우편번호를 검색하세요"
                    {...register('zipcode', { required: '우편번호를 입력하세요' })}
                    className="flex-1 bg-[#14141e] border-[#353535] text-[#e1e1e1] placeholder:text-[#6f6f7b] focus-visible:ring-0 focus-visible:ring-offset-0"
                  />
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => openPost({ onComplete: handleComplete })}
                    className="text-sm !rounded-md bg-[#191924] border border-[#444] text-[#dedede] hover:bg-[#242433]"
                  >
                    우편번호
                  </Button>
                </div>
                {errors.zipcode && <p className="text-xs text-red-400">{errors.zipcode.message}</p>}
              </div>

              <Field label="주소" error={errors.baseAddress?.message}>
                <Input
                  readOnly
                  placeholder="우편번호 검색 후 자동입력"
                  {...register('baseAddress', { required: '주소를 입력하세요' })}
                  className="bg-[#14141e] border-[#353535] text-[#e1e1e1] placeholder:text-[#6f6f7b] focus-visible:ring-0 focus-visible:ring-offset-0"
                />
              </Field>

              <Field label="상세 주소" error={errors.addressDetail?.message}>
                <Input
                  placeholder="건물, 동/호수 등"
                  {...register('addressDetail', { required: '상세 주소를 입력하세요' })}
                  className="bg-[#14141e] border-[#353535] text-[#e1e1e1] placeholder:text-[#6f6f7b] focus-visible:ring-0 focus-visible:ring-offset-0 focus:border-[#4b5cff]"
                />
              </Field>

              {mode === 'add' && (
                <div className="flex items-center gap-2">
                  <Controller
                    name="setAsDefault"
                    control={control}
                    render={({ field }) => (
                      <Checkbox
                        id="setAsDefault"
                        checked={!!field.value}
                        onCheckedChange={(v) => field.onChange(v === true)}
                        className="border-[#4b5cff]/40 data-[state=checked]:bg-[#4b5cff] data-[state=checked]:text-white"
                      />
                    )}
                  />
                  <Label htmlFor="setAsDefault" className="text-sm text-[#b5b5b5]">
                    기본 배송지로 설정
                  </Label>
                </div>
              )}

              <div className="flex justify-end gap-2 pt-2">
                <Button
                  type="submit"
                  disabled={isSubmitting || (mode === 'edit' && loadingDetail)}
                  className="text-sm !rounded-md bg-[#4b5cff] hover:bg-[#3a48d4] text-white"
                >
                  {mode === 'add' ? '저장' : '수정 저장'}
                </Button>
              </div>
            </form>
          </div>
        </div>

        {/* Footer */}
        <div className="px-5 py-3 border-t border-[#2a2a3b] flex justify-end rounded-b-xl">
          <Button
            variant="outline"
            onClick={onClose}
            className="text-sm !rounded-md bg-[#191924] border border-[#444] text-[#dedede] hover:bg-[#242433]"
          >
            닫기
          </Button>
        </div>
      </div>
    </div>
  );
}

/** 작은 필드 래퍼: 라벨/에러 색상 통일 */
function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-col gap-1">
      <Label className="text-sm text-[#b5b5b5]">{label}</Label>
      {children}
      {error && <p className="text-xs text-red-400">{error}</p>}
    </div>
  );
}
