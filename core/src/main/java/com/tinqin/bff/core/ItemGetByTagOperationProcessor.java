package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagOperation;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagRequest;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagWithPriceAndQuantityDataResponse;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagWithPriceAndQuantityResponse;
import com.tinqin.storage.api.operations.get.ItemGetByIdResponse;
import com.tinqin.storage.api.operations.getlistofitems.ItemGetListByIdsRequest;
import com.tinqin.storage.api.operations.getlistofitems.ItemGetListByIdsResponse;
import com.tinqin.storage.restexport.StorageRestClient;
import com.tinqin.zoostore.api.operations.item.getbytag.ItemGetByTagDataResponse;
import com.tinqin.zoostore.api.operations.item.getbytag.ItemGetByTagResponse;
import com.tinqin.zoostore.restexport.ZooStoreRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemGetByTagOperationProcessor implements ItemGetByTagOperation {

    private final ZooStoreRestClient zooStoreRestClient;
    private final StorageRestClient storageRestClient;

    @Autowired
    public ItemGetByTagOperationProcessor(ZooStoreRestClient zooStoreRestClient, StorageRestClient storageRestClient) {
        this.zooStoreRestClient = zooStoreRestClient;
        this.storageRestClient = storageRestClient;
    }

    @Override
    public ItemGetByTagWithPriceAndQuantityResponse process(ItemGetByTagRequest input) {
        ItemGetByTagResponse itemsFromZooStore = this.zooStoreRestClient.getItemsByTagTitle(input.getTitle(), input.getPageNumber(), input.getPageSize());

        List<String> itemIds = itemsFromZooStore
                .getItems()
                .stream()
                .map(ItemGetByTagDataResponse::getId)
                .toList();

        ItemGetListByIdsRequest requestToStorage = ItemGetListByIdsRequest
                .builder()
                .ids(itemIds)
                .build();

        ItemGetListByIdsResponse itemsFromStorage = this.storageRestClient.getCollectionOfItemsById(requestToStorage);

        List<ItemGetByTagWithPriceAndQuantityDataResponse> mappedItems = new ArrayList<>();

        for (ItemGetByTagDataResponse itemFromZooStore : itemsFromZooStore.getItems()) {
            for (ItemGetByIdResponse itemFromStorage : itemsFromStorage.getItems()) {
                if (itemFromStorage.getItemId().equals(itemFromZooStore.getId())) {
                    mappedItems.add(
                            ItemGetByTagWithPriceAndQuantityDataResponse
                                    .builder()
                                    .id(itemFromZooStore.getId())
                                    .title(itemFromZooStore.getTitle())
                                    .description(itemFromZooStore.getDescription())
                                    .vendor(itemFromZooStore.getVendor())
                                    .multimedia(itemFromZooStore.getMultimedia())
                                    .tags(itemFromZooStore.getTags())
                                    .price(itemFromStorage.getPrice())
                                    .quantity(itemFromStorage.getQuantity())
                                    .build()
                    );
                }
            }
        }

        return ItemGetByTagWithPriceAndQuantityResponse
                .builder()
                .items(mappedItems)
                .build();
    }
}
