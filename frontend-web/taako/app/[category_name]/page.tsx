import SearchInput from "@/components/atoms/Input/SearchInput"
import CardList from "@/components/cards/CardList"
import Filter from "@/components/filters/Filter"
import { filterOptions, itemsMap } from "@/components/filters/data"
interface CategoryPageProps {
  params: {
    category_name: string
  }
}

export default function CategoryPage({ params }: CategoryPageProps) {
  const { category_name } = params

  return (
    <div>
      <div className="default-container">
        <h1 className="text-center" style={{ marginBottom: '30px' }}>{category_name}</h1>
        <div className="flex items-center justify-between gap-4">
          <Filter filterOptions={filterOptions} itemsMap={itemsMap} />
          <SearchInput />
        </div>
        <CardList column={5} />
      </div>
    </div>
  )
}