package porunit.w8.realtydb.service;// porunit/w8/realtydb/web/mappers/ListingMappers.java

import porunit.w8.realtydb.data.Listing;
import porunit.w8.realtydb.data.ListingDto;
import porunit.w8.realtydb.data.PhotoMetaDto;

import java.util.List;

public class ListingMappers {
    public static ListingDto toDto(Listing l, List<PhotoMetaDto> photos) {
        return new ListingDto(
                l.getId(), l.getTitle(), l.getDescription(), l.getPrice(), l.getLocation(),
                l.getOwnership(), l.getEmail(), l.getCompanyName(), l.getPhone(),
                l.getEntrance(), l.getFloor(), l.getArea(), l.getCeilingHeight(), l.getFinishing(),
                l.getPowerKw(), l.getHeating(), l.getReadiness(), l.getBuildingType(),
                l.getRoadDistance(), l.getParking(), l.getDealType(), l.getLeased(),
                l.getTenant(), l.getMonthlyRent(), l.getAgentCommission(), l.getVatIncluded(),
                l.getCreatedAt(), l.getUpdatedAt(), photos
        );
    }
}
