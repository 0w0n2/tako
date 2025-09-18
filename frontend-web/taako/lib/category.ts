import api from "./api";

export const getMajorCategories = async () => {
    const res = await api.get("/v1/categories/majors");
    return res.data;
};

export const getMinorCategories = async (majorId:number) => {
    const res = await api.get(`/v1/categories/mediums/${majorId}`);
    return res.data;
}