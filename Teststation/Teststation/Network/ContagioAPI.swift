import Foundation
import Combine

enum ContagioAPI {
    static let accessToken = "<your key>"
    
    static func allPass() -> AnyPublisher<[PassInfo], TeststationError> {
        let url = URL(string: "https://efeu.local:13013/co_v1/pass/all")!
        
        let config = URLSessionConfiguration.default
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        config.urlCache = nil
        let session = URLSession(configuration: config)
        
        let urlRequest = URLRequest(url: url)
        
        let jsonDecoder = JSONDecoder()
        jsonDecoder.dateDecodingStrategy = .formatted(DateFormatter.iso8601Full)
        
        return session
            .dataTaskPublisher(for: urlRequest)
            .tryMap { response -> Data in
                guard
                    let httpURLResponse = response.response as? HTTPURLResponse,
                    httpURLResponse.statusCode == 200
                else {
                    throw TeststationError.statusCode
                }
                return response.data
            }
            .decode(type: [PassInfo].self, decoder: jsonDecoder)
            .mapError { TeststationError.map($0) }
            .eraseToAnyPublisher()
    }
    
    static func createPass(createPassRequest: CreatePassRequest) throws -> AnyPublisher<Data, TeststationError> {        
        let config = URLSessionConfiguration.default
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        config.urlCache = nil
        let session = URLSession(configuration: config)
        
        let url = URL(string: "https://efeu.local:13013/co_v1/pass")!
        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = "POST"
        
        let boundary = UUID().uuidString
        urlRequest.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        let payload = createPassRequest.toMultipart(boundary: boundary)
        
        urlRequest.httpBody = payload
        
        print("payload=\(payload)")
        
        return session
            .dataTaskPublisher(for: urlRequest)
            .tryMap { response -> Data in
                guard
                    let httpURLResponse = response.response as? HTTPURLResponse,
                    httpURLResponse.statusCode == 200
                else {
                    throw TeststationError.statusCode
                }
                return response.data
            }
            .mapError { TeststationError.map($0) }
            .eraseToAnyPublisher()
    }
}
