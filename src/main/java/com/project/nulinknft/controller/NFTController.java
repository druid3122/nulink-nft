package com.project.nulinknft.controller;

import com.project.nulinknft.dto.UserDTO;
import com.project.nulinknft.entity.NFT;
import com.project.nulinknft.entity.User;
import com.project.nulinknft.service.NFTService;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "nft")
@RestController
@RequestMapping("nft")
public class NFTController {

    private final NFTService nftService;

    public NFTController(NFTService nftService) {
        this.nftService = nftService;
    }

    @GetMapping("findByOwner")
    public ResponseEntity<List<NFT>> findByOwner(@RequestParam("owner") String owner){
        List<NFT> list = nftService.findByOwner(owner);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

}
