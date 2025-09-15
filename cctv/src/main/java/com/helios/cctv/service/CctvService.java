package com.helios.cctv.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.cctv.CctvApiDTO;
import com.helios.cctv.dto.cctv.CctvDTO;
import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import com.helios.cctv.dto.cluster.ClusterItem;
import com.helios.cctv.dto.cluster.RegionClusterRow;
import com.helios.cctv.entity.cctv.Cctv;
import com.helios.cctv.mapper.CctvMapper;
import com.helios.cctv.mapper.RegionMapper;
import com.helios.cctv.repository.CctvRepository;
import com.helios.cctv.util.CoordTransform;
import com.helios.cctv.util.CoordinateConverter;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.proj4j.ProjCoordinate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate5.support.OpenSessionInterceptor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CctvService {

    private final RegionMapper regionMapper;
    private final CctvMapper cctvMapper;
    private final CoordTransform coord;
    private final CctvRepository cctvRepository;


    @Value("${CCTV_API_KEY}")
    private String apiKey;

    //cctv 조회 -> controller 사용 X
    public ApiResponse<?> getCctv(GetCctvRequest request) {
        try {
            if (true){//(request.getLevel() <= 7) { //Detail 축소/확대값이 7보다 작거나 같을때
                List<CctvApiDTO> list = getCctvApi(request);
                return ApiResponse.ok(list,200);
            } else { //Cluster 위 조건문 외
                CoordTransform.Bounds b5186 = coord.to5186(request.getMinX(), request.getMinY(), request.getMaxX(), request.getMaxY());
                String boundsWkt = String.format(
                        java.util.Locale.US,
                        "POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))",
                        b5186.minX(), b5186.minY(),
                        b5186.maxX(), b5186.minY(),
                        b5186.maxX(), b5186.maxY(),
                        b5186.minX(), b5186.maxY(),
                        b5186.minX(), b5186.minY()
                );
                // 2) 시군구(구) 단위 대표점 + CCTV 개수 집계 (DB는 5186)
                List<RegionClusterRow> rows = regionMapper.findRegionClustersInBounds(boundsWkt);
                double tol = coord.simplifyToleranceMeters(request.getLevel());

                // 3) 대표점(5186) → WGS84 변환해서 응답
                List<ClusterItem> clusters = rows.stream().map(r -> {
                    CoordTransform.LngLat wgs = coord.to4326(r.getX(), r.getY()); // x,y: 5186
                    String geojson = coord.wkt5186ToGeoJson4326(r.getPolygonWkt(), tol);
                    return new ClusterItem(
                            r.getRegionId(),
                            r.getName(),
                            wgs.lat(), wgs.lng(),
                            r.getCount(),
                            geojson
                    );
                }).toList();

                return ApiResponse.ok(clusters,200);
            }

        } catch (Exception e) {
            return ApiResponse.fail("CCTV조회 실패",500);
        }
    }

    //cctv 조회 api
    public List<CctvApiDTO> getCctvApi(GetCctvRequest getCctvRequest) {
        //type 1 : 실시간 스트리밍
        //type 2 : 동영상
        //type 3 : 정지 영상
        //type 4 : 실시간 스트리밍 https
        //type 5 : 동영상 https
        //its 국도, ex 고속도로
        StringBuilder sb = new StringBuilder();
        try {
            String minX = Float.toString(getCctvRequest.getMinX());
            String maxX = Float.toString(getCctvRequest.getMaxX());
            String minY = Float.toString(getCctvRequest.getMinY());
            String maxY = Float.toString(getCctvRequest.getMaxY());
            String cctvType = Optional.ofNullable(getCctvRequest.getCctvType())
                    .filter(s -> !s.isBlank())
                    .orElse("4");
            String roadType = Optional.ofNullable((getCctvRequest.getRoadType()))
                    .filter(s -> !s.isBlank())
                    .orElse("ex");
            StringBuilder urlBuilder = new StringBuilder("https://openapi.its.go.kr:9443/cctvInfo");
            urlBuilder.append("?" + URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode(apiKey, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(roadType, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("cctvType", "UTF-8") + "=" + URLEncoder.encode(cctvType, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("minX", "UTF-8") + "=" + URLEncoder.encode(minX, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("maxX", "UTF-8") + "=" + URLEncoder.encode(maxX, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("minY", "UTF-8") + "=" + URLEncoder.encode(minY, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("maxY", "UTF-8") + "=" + URLEncoder.encode(maxY, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("getType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "text/xml;charset=UTF-8");

            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();

            // 1. JSON 문자열 받기
            String jsonString = sb.toString();

            // 2. Jackson 파서 준비
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonString);

            System.out.println(rootNode);
            // 3. data 배열만 추출
            JsonNode dataArray = rootNode.path("response").path("data");

            // 4. DTO 리스트로 변환
            List<CctvApiDTO> cctvList = new ArrayList<>();
            for (JsonNode node : dataArray) {
                CctvApiDTO dto = new CctvApiDTO();
                dto.setRoadsectionid(node.path("roadsectionid").asText());
                dto.setFilecreatetime(node.path("filecreatetime").asText());
                dto.setCctvtype(String.valueOf(node.path("cctvtype").asInt())); // int → String
                dto.setCctvurl(node.path("cctvurl").asText());
                dto.setCctvresolution(node.path("cctvresolution").asText());
                dto.setCoordx(String.valueOf(node.path("coordx").asDouble())); // double → String
                dto.setCoordy(String.valueOf(node.path("coordy").asDouble()));
                dto.setCctvformat(node.path("cctvformat").asText());
                dto.setCctvname(node.path("cctvname").asText());

                cctvList.add(dto);
            }

            // 이제 여기서 cctvList를 반환하면 됩니다
            return cctvList;
        } catch (Exception e) {
            return null;
        }
    }

    //cctv 저장
    public ApiResponse<Void> saveCctv(GetCctvRequest getCctvRequest) {
        try{
            List<CctvApiDTO> cctvApiList = getCctvApi(getCctvRequest);
            for (CctvApiDTO dto : cctvApiList) {
                double coordx = Double.parseDouble(dto.getCoordx());
                double coordy = Double.parseDouble(dto.getCoordy());

                // 1. 위경도 좌표 → EPSG:5186 변환
                ProjCoordinate tmCoord = CoordinateConverter.to5186(coordx, coordy);

                // 2. Point 객체 생성 (JTS)
                GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 5186);
                Point point = geometryFactory.createPoint(new Coordinate(tmCoord.x, tmCoord.y));
                point.setSRID(5186);

                // 3. WKT 생성
                String wkt = new WKTWriter().write(point);

                // 포함된 Region 찾기
                Integer regionId = regionMapper.findRegionIdByPoint(wkt);  // null일 수 있음


                // CCTV 저장
                CctvDTO cctv = new CctvDTO();
                cctv.setLocation(dto.getCctvname());
                cctv.setLatitude(coordy);    // 위도
                cctv.setLongitude(coordx);   // 경도
                cctv.setPoint(point);
                cctv.setWkt(wkt);
                cctv.setRegionId(regionId);

                cctvMapper.insert(cctv);
            }
            return ApiResponse.ok(null,200);
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 전체 오류 스택 출력
            //log.error("insert 오류 발생", e); // 로그로도 출력
            return ApiResponse.fail("저장 실패",500);
        }
    }

    //cctv 조회 from DB
    public ApiResponse<List<CctvApiDTO>> findInBoundsAsDto(GetCctvRequest request) {
        try{
            double xMin = Math.min(request.getMinX(), request.getMaxX());
            double xMax = Math.max(request.getMinX(), request.getMaxX());
            double yMin = Math.min(request.getMinY(), request.getMaxY());
            double yMax = Math.max(request.getMinY(), request.getMaxY());

            List<Cctv> entities = cctvRepository.findWithinBoundsByRoadType(xMin, xMax, yMin, yMax,"EX"); //추가

            List<CctvApiDTO> list = entities.stream()
                    .map(this::toDto)
                    .toList();
            return ApiResponse.ok(list,200);
        } catch (Exception e) {
            return ApiResponse.fail("조회실패",500);
        }
    }

    //cctv검색
    public ApiResponse<List<CctvApiDTO>> search(String search){
        if (search == null || search.isBlank()) return null;
        try{
            List<Cctv> entities = cctvRepository.searchExByName(search);
            List<CctvApiDTO> list = entities.stream()
                    .map(this::toDto)
                    .toList();
            return ApiResponse.ok(list,200);
        } catch (Exception e) {
            return ApiResponse.fail("검색실패",500);
        }
        
    }

    //Entity -> DTO 변환
    private CctvApiDTO toDto(Cctv c) {
        CctvApiDTO dto = new CctvApiDTO();
        dto.setRoadsectionid(nvl(c.getRoadsectionid()));
        dto.setFilecreatetime(nvl(c.getFilecreatetime()));
        dto.setCctvtype(nvl(c.getCctvtype()));
        dto.setCctvurl(nvl(c.getCctvurl()));
        dto.setCctvresolution(nvl(c.getCctvresolution()));
        dto.setCoordx(formatCoord(c.getCoordx()));   // Double → 문자열(소수 6자리)
        dto.setCoordy(formatCoord(c.getCoordy()));
        dto.setCctvformat(nvl(c.getCctvformat()));
        dto.setCctvname(nvl(c.getCctvname()));
        return dto;
    }

    //null -> "" 변환
    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "" : s;
    }

    //BigDecimal -> String 변환
    private String formatCoord(BigDecimal v) {
         return (v == null) ? "" : v.setScale(6, RoundingMode.HALF_UP).toPlainString();
    }






}
