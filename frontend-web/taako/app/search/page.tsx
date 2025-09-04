'use client';

import { useSearchParams } from 'next/navigation';

export default function Search() {
    const searchParams = useSearchParams();
    const query = searchParams.get('name') || '';

    return (
        <div className="default-container">
            {query && <p>'{query}' 검색결과</p>}
        </div>
    );
}