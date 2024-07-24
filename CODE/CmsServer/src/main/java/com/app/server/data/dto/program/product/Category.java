package com.app.server.data.dto.program.product;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Category {
    private int id;
    private String name;
    private String image;
    private int isBranch;
    private int parentId;
    private int priority;
    private int status;
    private Date createdDate;
    private int itemType;
    private int level;
    private int parentPriority;
    private boolean activatedStatus; // depends on parent
    private Category parent;
    private List<Category> ltChild;
    private List<ProductGroup> ltProductGroup;

    public Category() {
        name = "";
        image = "";
        ltChild = new ArrayList<>();
        ltProductGroup = new ArrayList<>();
    }

    // set data
    public void setData(Category category) {
        this.name = category.getName();
        this.image = category.getImage();
        this.isBranch = category.getIsBranch();
        this.parentId = category.parentId;
        this.priority = category.getPriority();
        this.status = category.getStatus();
        this.createdDate = category.getCreatedDate();
        this.itemType = category.getItemType();
        this.level = category.getLevel();
        this.parentPriority = category.getParentPriority();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Category category = (Category) o;
        return id == category.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}