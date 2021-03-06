package com.zanygeek.rememberme.repository;

import com.zanygeek.rememberme.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    List<Photo> findAllByMemorialId(int memorialId);
    Photo findByMemorialIdAndMainIsTrue(int memorial_id);
    Photo findAllByObituaryId(int obituaryId);
    Photo findByUrl(String Url);
    Photo findByObituaryId(int obituaryId);
}
