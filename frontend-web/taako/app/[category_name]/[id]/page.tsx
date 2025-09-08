interface CategoryItemPageProps {
  params: {
    category_name: string;
    id: string;
  };
}

export default function CategoryItemPage({ params }: CategoryItemPageProps) {
  const { category_name, id } = params;

  return (
    <div>
      <h1>{category_name} 상품 상세</h1>
      {/* 상품 상세 정보 컴포넌트 */}
      <p>카테고리: {category_name}</p>
      <p>상품 ID: {id}</p>
    </div>
  );
}