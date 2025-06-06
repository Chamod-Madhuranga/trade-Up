package com.example.beststore.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.beststore.models.Product;
import com.example.beststore.models.ProductDto;
import com.example.beststore.services.ProductRepository;

import jakarta.validation.Valid;



@Controller
@RequestMapping("/products")
public class ProductController {
	@Autowired
    private ProductRepository repo;

    @GetMapping({"", "/"})
    public String showProductList (Model model) {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC,"id"));
        System.out.println(products);
        model.addAttribute("products", products);
        return "products/index";
    }
    @GetMapping("/create")
    public String showCreatePage (Model model) { 
    	ProductDto productDto = new ProductDto(); 
    	model.addAttribute("productDto", productDto); 
    	return "products/CreateProduct";
    }
    
    @PostMapping("/create")
    public String createProduct(
    		@Valid @ModelAttribute ProductDto productDto,
    		BindingResult result
    		)
    
    {
    	if(productDto.getImageFile().isEmpty()) {
    		result.addError(new FieldError("productDto","imageFile","The image file is required"));
    	}
    	
    	if (result.hasErrors()) {
    		return "products/CreateProduct";
    	}
    	
    	
    	//save image file
    	
    	MultipartFile image =productDto.getImageFile();
    	Date createdAt =new  Date(0);
    	String storageFileName =createdAt.getTime() + "_" + image.getOriginalFilename();
    	try {
    		String uploadDir="public/images/";
    		Path uploadPath = Paths.get(uploadDir);
    		
    		if(!Files.exists(uploadPath)) {
    			Files.createDirectories(uploadPath);
    		}
    		try (InputStream InputStream=image.getInputStream()){
    			Files.copy(InputStream,Paths.get(uploadDir,storageFileName),
    					StandardCopyOption.REPLACE_EXISTING);
    			
    		}
    	}
    	
    	catch(Exception ex) {
    		System.out.println("Exception:" + ex.getMessage());
    	}
    	Product product =new Product();
    	
    	product.setName(productDto.getName());
    	product.setBrand(productDto.getBrand());
    	product.setCategory(productDto.getCategory());
    	product.setPrice(productDto.getPrice());
    	product.setDescription(productDto.getDescription());
    	product.setCreatedAt(createdAt);
    	product.setImageFilename(storageFileName);
    	
    	repo.save(product);
    	return "redirect:/products";
    }
    
    @GetMapping("/edit")
    public String showEditPage(
    		Model model,
    		@RequestParam int id) {
    	
    	try {
    		Product product =repo.findById(id).get();
    		model.addAttribute("product",product);
    		
    		ProductDto productDto =new ProductDto();
    		productDto.setName(product.getName());
        	productDto.setBrand(product.getBrand());
        	productDto.setCategory(product.getCategory());
        	productDto.setPrice(product.getPrice());
        	productDto.setDescription(product.getDescription());
    		
        	model.addAttribute("productDto",productDto);
    		
    	}catch(Exception ex){
    		
    		System.out.println("Exception:" + ex.getMessage());
    		return "redirect:/products";
    	}
    	return "products/EditProduct";
    }
    
    @PostMapping("/edit")
    public String updateProduct(
    		Model model,
    		@RequestParam int id,
    		@Valid @ModelAttribute ProductDto productDto,
    		BindingResult result
    		) {
    	
    	try {
    		Product product =repo.findById(id).get();
    		model.addAttribute("product",product);
    		
    		if(result.hasErrors()) {
    			return "products/EditProduct";
    		}
    		
    		if(!productDto.getImageFile().isEmpty()) {
    			
    			String uploadDir= "public/images/";
    			Path oldImagePath= Paths.get(uploadDir + product.getImageFilename());
    			
    			try {
    				Files.delete(oldImagePath);
    			}catch(Exception ex){
    				System.out.println ("Exception: " + ex.getMessage());
    			}
    			
    			//save image file
    	    	
    	    	MultipartFile image =productDto.getImageFile();
    	    	Date createdAt =new  Date(0);
    	    	String storageFileName =createdAt.getTime() + "_" + image.getOriginalFilename();
    	    	
    	    	try (InputStream InputStream=image.getInputStream()){
        			Files.copy(InputStream,Paths.get(uploadDir,storageFileName),
        					StandardCopyOption.REPLACE_EXISTING);
        			
        		}
    	    	product.setImageFilename(storageFileName);
    		}
    		product.setName(productDto.getName());
        	product.setBrand(productDto.getBrand());
        	product.setCategory(productDto.getCategory());
        	product.setPrice(productDto.getPrice());
        	product.setDescription(productDto.getDescription());
        	
        	
        	repo.save(product);
    		
    		
    	}catch(Exception ex){
    		
    		System.out.println("Exception:" + ex.getMessage());
    		
    	}
    	return "redirect:/products";
    }
    
    
    @GetMapping ("/delete")
    public String deleteProduct(
    		@RequestParam int id
    		) {
    	
    	try {
    		Product product = repo.findById(id).get();
    		
    		Path imagePath =Paths.get("public/images/" + product.getImageFilename());
    		
    		try {
    			Files.delete(imagePath);
    		}
    		catch(Exception ex) {
    			System.out.println(" Exception: "+ ex.getMessage());
    		}
    		repo.delete(product);
    		
    	}
    	catch(Exception ex) {
    		
    		System.out.println(" Exception: "+ ex.getMessage());
    	}
    	return "redirect:/products";
    	
    }

}
