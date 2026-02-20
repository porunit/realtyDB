package porunit.w8.realtydb.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import porunit.w8.realtydb.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ListingRequest(
        @NotBlank String title,
        String description,
        @Positive BigDecimal price,
        String location,
        @NotBlank String ownerName,
        OwnershipType ownershipType,
        @Email String email,
        String companyName,
        String phone,
        EntranceType entranceType,
        Integer floor,
        Double area,
        Double ceilingHeight,
        Finishing finishing,
        Integer powerKw,
        Heating heating,
        BuildingType buildingType,
        String developerName,
        Integer floorsCount,
        RoadDistance roadDistance,
        Parking parkingType,
        Integer parkingTotalSpaces,
        Integer parkingAvailableSpaces,
        BigDecimal parkingHourlyRate,
        BigDecimal parkingMonthlyRate,
        DealType dealType,
        String contractNumber,
        LocalDate contractDate,
        BigDecimal commissionPercent,
        Readiness readinessType,
        LocalDate expectedCompletion,
        LocalDate actualCompletion,
        String currentStage,
        Integer completionPercent,
        Boolean leased,
        String tenant,
        BigDecimal monthlyRent,
        Boolean agentCommission,
        Boolean vatIncluded
) {}
