package com.jianquanwang.ecommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private List<AddressDTO> content = new ArrayList<>();
}
