package nextstep.subway.application;

import java.util.List;
import java.util.stream.Collectors;
import javassist.NotFoundException;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.LineRepository;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.StationRepository;
import nextstep.subway.dto.LineRequest;
import nextstep.subway.dto.LineResponse;
import nextstep.subway.exception.DownStationNotFoundException;
import nextstep.subway.exception.UpStationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LineService {

    private LineRepository lineRepository;
    private StationRepository stationRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        validateExistOrThrow(lineRequest);
        Station upStation = findStation(lineRequest.getUpStationId(),
            new UpStationNotFoundException());
        Station downStation = findStation(lineRequest.getDownStationId(),
            new DownStationNotFoundException());
        Line line = new Line(lineRequest.getName(), lineRequest.getColor(),
            new Section(upStation, downStation,
                lineRequest.getDistance()));
        lineRepository.save(line);
        return LineResponse.of(line);
    }

    private Station findStation(Long stationId, RuntimeException e) {
        return stationRepository.findById(stationId)
            .orElseThrow(() -> new RuntimeException(e));
    }

    public List<LineResponse> findAllLine() {
        List<Line> lines = lineRepository.findAll();
        return lines.stream().map(line -> LineResponse.of(line)).collect(Collectors.toList());
    }

    public LineResponse findLine(Long id) throws NotFoundException {
        Line line = findLineOrThrow(id);
        return LineResponse.of(line);
    }

    @Transactional
    public void updateLine(Long id, LineRequest lineRequest) throws NotFoundException {
        Line line = findLineOrThrow(id);
        line.update(lineRequest);
    }

    @Transactional
    public void deleteLine(Long id) throws NotFoundException {
        findLineOrThrow(id);
        lineRepository.deleteById(id);
    }


    private void validateExistOrThrow(LineRequest lineRequest) {
        if (lineRepository.findByName(lineRequest.getName()) != null) {
            throw new IllegalArgumentException("이미 존재하는 노선입니다");
        }
    }

    public Line findLineOrThrow(Long id) throws NotFoundException {
        return lineRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("지하철 노선이 존재하지 않습니다."));
    }
}