package org.solcation.solcation_be.domain.stats;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.dto.FinishTravelListDTO;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.TravelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TravelRepository travelRepository;

    public List<FinishTravelListDTO> getFinishedTravels(Long groupPk) {
        List<Travel> travels =
                travelRepository.findByGroup_GroupPkAndTpStateOrderByTpEndDesc(groupPk, TRAVELSTATE.FINISH);

        return travels.stream()
                .map(t -> FinishTravelListDTO.builder()
                        .tpTitle(t.getTpTitle())
                        .tpLocation(t.getTpLocation())
                        .tpStart(t.getTpStart())
                        .tpEnd(t.getTpEnd())
                        .tpImage(t.getTpImage())
                        .tpcIcon(t.getTravelCategory().getTpcIcon())
                        .build())
                .toList();
    }
}
