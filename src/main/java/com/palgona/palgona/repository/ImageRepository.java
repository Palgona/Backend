package com.palgona.palgona.repository;

import com.palgona.palgona.domain.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("""
        select i
        from Image i
        where i.imageUrl in :urls
    """)
    List<Image> findImageByImageUrls(List<String> urls);

    @Query("""
     delete from Image i
     where i.imageUrl in :urls
    """)
    void deleteByImageUrls(List<String> urls);
}