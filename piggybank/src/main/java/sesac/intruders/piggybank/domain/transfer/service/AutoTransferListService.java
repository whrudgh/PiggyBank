// 2. Service 수정
package sesac.intruders.piggybank.domain.transfer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesac.intruders.piggybank.domain.autotransfer.model.Autotransfer;
import sesac.intruders.piggybank.domain.transfer.repository.AutotransferListRepository;
import sesac.intruders.piggybank.domain.transfer.dto.response.AutoTransferListResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoTransferListService {
    private final AutotransferListRepository autotransferRepository;

    public List<AutoTransferListResponse> getAutoTransferList(UUID userCode) {
        List<Autotransfer> autotransfers = autotransferRepository.findByUserCodeAndIsRecurringTrue(userCode);
        return autotransfers.stream()
                .map(AutoTransferListResponse::from)
                .collect(Collectors.toList());
    }
}