interface CategoryPageProps {
  params: {
    category_name: string;
  };
}

export default function CategoryPage({ params }: CategoryPageProps) {
  const { category_name } = params;

  return (
    <div>
      <h1>{category_name} 카테고리</h1>
      {/* 카테고리별 상품 목록 컴포넌트 */}
      <p>카테고리: {category_name}</p>
    </div>
  );
}
