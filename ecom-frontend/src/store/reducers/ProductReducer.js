const intialState={
    products: null,
    categories: null,
    pagination: {},
};

export const productReducer = (state = intialState, action) => {
    switch (action.type) {
        case "FETCH PRODUCTS":
            return {
                ...state,
                products: action.payload,
                pagination: {
                    ...state.pagination,
                    pageNumber: action.pageNumber,
                    pageSize: action.pageSize,
                    totalElements: action.totalElements,
                    totalPages: action.totalPages,
                    lastPage: action.lastPage,
                }
            };
        default:
            return state;
    }
};