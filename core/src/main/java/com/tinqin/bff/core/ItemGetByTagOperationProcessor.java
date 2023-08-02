package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagOperation;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagRequest;
import com.tinqin.bff.api.operations.item.getbytag.ItemWithPriceAndQuantityDataResponse;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagWithPriceAndQuantityResponse;
import com.tinqin.storage.api.operations.get.ItemGetByIdResponse;
import com.tinqin.storage.api.operations.getlistofitems.ItemGetListByIdsRequest;
import com.tinqin.storage.api.operations.getlistofitems.ItemGetListByIdsResponse;
import com.tinqin.storage.restexport.StorageRestClient;
import com.tinqin.zoostore.api.operations.item.getbytag.ItemGetByTagResponse;
import com.tinqin.zoostore.api.operations.item.getbytag.ItemGetDataResponse;
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
                .map(ItemGetDataResponse::getId)
                .toList();

        ItemGetListByIdsRequest requestToStorage = ItemGetListByIdsRequest
                .builder()
                .ids(itemIds)
                .build();

        ItemGetListByIdsResponse itemsFromStorage = this.storageRestClient.getCollectionOfItemsById(requestToStorage);

        List<ItemWithPriceAndQuantityDataResponse> mappedItems = new ArrayList<>();

        for (ItemGetDataResponse itemFromZooStore : itemsFromZooStore.getItems()) {
            itemsFromStorage
                    .getItems()
                    .stream()
                    .filter(
                            itemFromStorage -> itemFromStorage
                                    .getItemId()
                                    .equals(itemFromZooStore.getId())
                    )
                    .map(itemFromStorage -> mapToItemFromStorageAndZooStoreData(itemFromZooStore, itemFromStorage))
                    .forEach(mappedItems::add);
        }

        return ItemGetByTagWithPriceAndQuantityResponse
                .builder()
                .items(mappedItems)
                .build();
    }

    private ItemWithPriceAndQuantityDataResponse mapToItemFromStorageAndZooStoreData(
            ItemGetDataResponse zooStoreItem,
            ItemGetByIdResponse storageItem
    ) {
        return ItemWithPriceAndQuantityDataResponse
                .builder()
                .id(zooStoreItem.getId())
                .title(zooStoreItem.getTitle())
                .description(zooStoreItem.getDescription())
                .vendor(zooStoreItem.getVendor())
                .multimedia(zooStoreItem.getMultimedia())
                .tags(zooStoreItem.getTags())
                .price(storageItem.getPrice())
                .quantity(storageItem.getQuantity())
                .build();
    }
}
