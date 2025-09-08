package com.helios.cctv.repository;

import com.helios.cctv.dto.cctv.CctvMini;
import com.helios.cctv.entity.cctv.Cctv;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CctvRepository extends JpaRepository<Cctv, Long> {
    @Query("""
           select c
             from Cctv c
            where c.coordx is not null
              and c.coordy is not null
              and c.coordx between :minX and :maxX
              and c.coordy between :minY and :maxY
           """)
    List<Cctv> findWithinBounds(@Param("minX") Double minX,
                                @Param("maxX") Double maxX,
                                @Param("minY") Double minY,
                                @Param("maxY") Double maxY);

    @Query("""
       select c from Cctv c
       where c.coordx is not null and c.coordy is not null
         and c.roadType = :roadType
         and c.coordx between :minX and :maxX
         and c.coordy between :minY and :maxY
       """)
    List<Cctv> findWithinBoundsByRoadType(@Param("minX") Double minX,
                                          @Param("maxX") Double maxX,
                                          @Param("minY") Double minY,
                                          @Param("maxY") Double maxY,
                                          @Param("roadType") String roadType);

    @Query("""
       select c from Cctv c
       where c.cctvname = :cctvname and c.coordx = :coordx
       """)
    List<Cctv> findByCctvnameAndCoordx(@Param("cctvname") String cctvname,
                                       @Param("coordx") java.math.BigDecimal coordx);

    @Query("""
       select c.id as id, c.cctvurl as cctvurl
       from Cctv c
       where c.roadType = :roadType
       order by c.id
    """)
    Page<CctvMini> findByRoadTypeMini(@Param("roadType") String roadType, Pageable pageable);
}

