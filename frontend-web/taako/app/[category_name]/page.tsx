import CardList from "@/components/cards/CardList";

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
      <CardList />
    </div>
  );
}