package ru.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDetailsDto {
    private Long collectionId;
    private Long ownerUserId;
    private String ownerUsername;
    private String name;
    private String description;
    private List<CollectionBookItemDto> books;
}
