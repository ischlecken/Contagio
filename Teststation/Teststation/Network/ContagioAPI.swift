import Foundation
import Combine

enum ContagioAPI {
    static let contagioBaseURL = "https://efeu.local:13013/co_v1"
    
    
    static func getTeststations() -> AnyPublisher<[Teststation], TeststationError> {
        let url = URL(string: "\(contagioBaseURL)/teststations")!
        
        return createUrlSession()
            .dataTaskPublisher(for: URLRequest(url: url))
            .tryMap { response -> Data in
                guard
                    let httpURLResponse = response.response as? HTTPURLResponse,
                    httpURLResponse.statusCode == 200
                else {
                    throw TeststationError.statusCode
                }
                return response.data
            }
            .decode(type: [Teststation].self, decoder: createJsonDecoder())
            .mapError { TeststationError.map($0) }
            .eraseToAnyPublisher()
    }
    
    static func getTester() -> AnyPublisher<[Tester], TeststationError> {
        let url = URL(string: "\(contagioBaseURL)/tester")!
        
        return createUrlSession()
            .dataTaskPublisher(for: URLRequest(url: url))
            .tryMap { response -> Data in
                guard
                    let httpURLResponse = response.response as? HTTPURLResponse,
                    httpURLResponse.statusCode == 200
                else {
                    throw TeststationError.statusCode
                }
                return response.data
            }
            .decode(type: [Tester].self, decoder: createJsonDecoder())
            .mapError { TeststationError.map($0) }
            .eraseToAnyPublisher()
    }
    
    
    static func allPass() -> AnyPublisher<[PassInfo], TeststationError> {
        let url = URL(string: "\(contagioBaseURL)/pass")!
        
        return createUrlSession()
            .dataTaskPublisher(for: URLRequest(url: url))
            .tryMap { response -> Data in
                guard
                    let httpURLResponse = response.response as? HTTPURLResponse,
                    httpURLResponse.statusCode == 200
                else {
                    throw TeststationError.statusCode
                }
                return response.data
            }
            .decode(type: [PassInfo].self, decoder: createJsonDecoder())
            .mapError { TeststationError.map($0) }
            .eraseToAnyPublisher()
    }
    
    
    static func createPass(createPassRequest: CreatePassRequest) throws -> AnyPublisher<PassInfo, TeststationError> {
        let url = URL(string: "\(contagioBaseURL)/pass")!
        
        var urlRequest = URLRequest(url: url)
        
        urlRequest.httpMethod = "POST"
        let boundary = "Boundary-" + UUID().uuidString
        urlRequest.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        let payload = createPassRequest.toMultipart(boundary: boundary)
        urlRequest.httpBody = payload
        urlRequest.setValue(String(payload.count), forHTTPHeaderField: "Content-Length")
        
        //print(String(decoding: payload, as: UTF8.self))
        
        return createUrlSession()
            .dataTaskPublisher(for: urlRequest)
            .tryMap { response -> Data in
                guard
                    let httpURLResponse = response.response as? HTTPURLResponse,
                    httpURLResponse.statusCode == 200 || httpURLResponse.statusCode == 201
                else {
                    throw TeststationError.statusCode
                }
                
                return response.data
            }
            .decode(type: PassInfo.self, decoder: createJsonDecoder())
            .mapError { TeststationError.map($0) }
            .eraseToAnyPublisher()
    }
    
    static func getPass(passId: String) throws -> AnyPublisher<Data, TeststationError> {
        let url = URL(string: "\(contagioBaseURL)/pass/\(passId)")!
        
        return createUrlSession()
            .dataTaskPublisher(for:  URLRequest(url: url))
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
    
    private static func createUrlSession() -> URLSession {
        let config = URLSessionConfiguration.default
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        config.urlCache = nil
        
        return URLSession(configuration: config)
    }
    
    private static func createJsonDecoder() ->JSONDecoder {
        let jsonDecoder = JSONDecoder()
        jsonDecoder.dateDecodingStrategy = .formatted(DateFormatter.iso8601Full)
        
        return jsonDecoder;
    }
}
