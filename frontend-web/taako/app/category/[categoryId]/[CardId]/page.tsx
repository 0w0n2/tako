export default function CategoryItemPage({ params }: { params: { categoryId: string, CardId: string } }) {
  return (
    <div>
      <h1>Category Item Page categoryId: {params.categoryId} CardId: {params.CardId}</h1>
    </div>
  );
}