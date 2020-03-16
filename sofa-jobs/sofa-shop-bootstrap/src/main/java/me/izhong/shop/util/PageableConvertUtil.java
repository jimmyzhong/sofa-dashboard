package me.izhong.shop.util;

import me.izhong.common.domain.PageRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.springframework.data.domain.PageRequest.of;

public class PageableConvertUtil {

    public static Pageable toDataPageable(PageRequest request) {
        Sort sort = Sort.unsorted();
        if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getOrderDirection())) {
            sort = Sort.by("asc".equalsIgnoreCase(request.getOrderDirection()) ? Sort.Direction.ASC: Sort.Direction.DESC,
                    request.getOrderByColumn());
        }

        return of(
                Long.valueOf(request.getPageNum()-1).intValue(),
                Long.valueOf(request.getPageSize()).intValue(), sort);
    }
}
