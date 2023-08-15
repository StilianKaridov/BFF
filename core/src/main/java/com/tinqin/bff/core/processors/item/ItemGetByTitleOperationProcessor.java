package com.tinqin.bff.core.processors.item;

import com.tinqin.bff.api.operations.item.getbytag.ItemWithPriceAndQuantityDataResponse;
import com.tinqin.bff.api.operations.item.getbytitle.ItemGetByItemTitleRequest;
import com.tinqin.bff.api.operations.item.getbytitle.ItemGetByItemTitleResponse;
import com.tinqin.bff.api.operations.item.getbytitle.ItemGetByTitleOperation;
import com.tinqin.bff.core.exception.NoSuchItemException;
import com.tinqin.storage.api.operations.get.ItemGetByIdResponse;
import com.tinqin.storage.restexport.StorageRestClient;
import com.tinqin.zoostore.api.operations.item.getbytag.ItemGetDataResponse;
import com.tinqin.zoostore.api.operations.item.getbytitle.ItemGetByTitleResponse;
import com.tinqin.zoostore.restexport.ZooStoreRestClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemGetByTitleOperationProcessor implements ItemGetByTitleOperation {

    private final ZooStoreRestClient zooStoreRestClient;
    private final StorageRestClient storageRestClient;

    @Autowired
    public ItemGetByTitleOperationProcessor(ZooStoreRestClient zooStoreRestClient, StorageRestClient storageRestClient) {
        this.zooStoreRestClient = zooStoreRestClient;
        this.storageRestClient = storageRestClient;
    }

    @Override
    public ItemGetByItemTitleResponse process(ItemGetByItemTitleRequest input) {
        ItemGetByTitleResponse itemsFromZooStore = this.zooStoreRestClient.getItemsByItemTitle(input.getTitle(), input.getPageNumber(), input.getPageSize());

        List<String> itemIds = itemsFromZooStore
                .getItems()
                .stream()
                .map(ItemGetDataResponse::getId)
                .toList();

        List<ItemGetByIdResponse> itemsFromStorage;

        try {
            itemsFromStorage = itemIds
                    .parallelStream()
                    .map(storageRestClient::getItemById)
                    .toList();
        } catch (FeignException ex) {
            throw new NoSuchItemException();
        }

        List<ItemWithPriceAndQuantityDataResponse> mappedItems = new ArrayList<>();

        for (ItemGetDataResponse itemFromZooStore : itemsFromZooStore.getItems()) {
            itemsFromStorage
                    .stream()
                    .filter(
                            itemFromStorage -> itemFromStorage
                                    .getItemId()
                                    .equals(itemFromZooStore.getId())
                    )
                    .map(itemFromStorage -> mapToItemFromStorageAndZooStoreData(itemFromZooStore, itemFromStorage))
                    .forEach(mappedItems::add);
        }

        return ItemGetByItemTitleResponse
                .builder()
                .limit(itemsFromZooStore.getLimit())
                .page(itemsFromZooStore.getPage())
                .totalItems((long) mappedItems.size())
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
