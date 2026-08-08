package com.codingfactory.maintrack.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// Mia "selida" apotelesmaton: to periexomeno kai oi plirofories gia ti selidopoiisi.
//
// Giati diko mas antikeimeno kai oxi to Page tou Spring: to Page epistrefei poly
// perissoteres plirofories apo oses xreiazetai to frontend (paginationInfo, sort,
// pageable klp) kai to schima tou allazei metaxi ekdoseon tou Spring. Auto edo
// einai stathero kai kathara.
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public PageResponse() {
    }

    // Metatrepei ena Page<Entity> se PageResponse<Dto>
    public static <E, D> PageResponse<D> from(Page<E> page, Function<E, D> mapper) {
        PageResponse<D> response = new PageResponse<>();
        response.content = page.getContent().stream().map(mapper).toList();
        response.page = page.getNumber();
        response.size = page.getSize();
        response.totalElements = page.getTotalElements();
        response.totalPages = page.getTotalPages();
        response.first = page.isFirst();
        response.last = page.isLast();
        return response;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
