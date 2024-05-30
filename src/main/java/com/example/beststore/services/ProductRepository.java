package com.example.beststore.services;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.beststore.models.Product;

public interface ProductRepository extends JpaRepository <Product, Integer>{

}

