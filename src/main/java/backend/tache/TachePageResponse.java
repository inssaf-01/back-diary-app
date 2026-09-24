package backend.tache;
import java.util.List;
public record TachePageResponse(List<TacheResponse> content, long totalElements, int totalPages, int number, int size) {}
