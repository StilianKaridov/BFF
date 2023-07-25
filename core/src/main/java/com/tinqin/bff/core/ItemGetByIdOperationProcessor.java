package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.item.ItemGetByIdOperation;
import com.tinqin.bff.api.operations.item.ItemRequest;
import com.tinqin.bff.api.operations.item.ItemResponse;
import com.tinqin.storage.api.operations.get.ItemGetByIdResponse;
import com.tinqin.storage.restexport.StorageRestClient;
import com.tinqin.zoostore.api.operations.item.get.GetItemByIdResponse;
import com.tinqin.zoostore.restexport.ZooStoreRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemGetByIdOperationProcessor implements ItemGetByIdOperation {

    private final StorageRestClient storageRestClient;
    private final ZooStoreRestClient zooStoreRestClient;

    @Autowired
    public ItemGetByIdOperationProcessor(StorageRestClient storageRestClient, ZooStoreRestClient zooStoreRestClient) {
        this.storageRestClient = storageRestClient;
        this.zooStoreRestClient = zooStoreRestClient;
    }

    @Override
    public ItemResponse process(ItemRequest input) {
        String itemId = input.getId();

        ItemGetByIdResponse itemFromStorage = this.storageRestClient.getItemById(itemId);
        GetItemByIdResponse itemFromZooStore = this.zooStoreRestClient.getItemById(itemId);

        return ItemResponse
                .builder()
                .itemId(itemId)
                .title(itemFromZooStore.getTitle())
                .price(itemFromStorage.getPrice())
                .quantity(itemFromStorage.getQuantity())
                .build();
    }
}
