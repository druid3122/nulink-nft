package com.project.nulinknft.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "nft")
public class NFT extends BaseEntity{

    @Column(name = "tx_hash", nullable = false)
    private String txHash;

    private String owner;

    private int tokenId;

    private int airdropLevel;


    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public int getAirdropLevel() {
        return airdropLevel;
    }

    public void setAirdropLevel(int airdropLevel) {
        this.airdropLevel = airdropLevel;
    }
}
