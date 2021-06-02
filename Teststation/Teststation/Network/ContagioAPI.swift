import Foundation
import Combine

enum ContagioAPI {
    
    static func getTeststations() -> AnyPublisher<[Teststation], TeststationError> {
        let url = URL(string: "\(EnvConfig.contagioapiBaseURL)/teststations")!
        
        return createUrlSession()
            .dataTaskPublisher(for: createURLRequest(url:url))
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
        let url = URL(string: "\(EnvConfig.contagioapiBaseURL)/tester")!
        
        return createUrlSession()
            .dataTaskPublisher(for: createURLRequest(url:url))
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
        let url = URL(string: "\(EnvConfig.contagioapiBaseURL)/pass")!
        
        return createUrlSession()
            .dataTaskPublisher(for: createURLRequest(url:url))
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
    
    
    static func getPass(passId: String) throws -> AnyPublisher<Data, TeststationError> {
        let url = URL(string: "\(EnvConfig.contagioapiBaseURL)/pass/\(passId)")!
        
        return createUrlSession()
            .dataTaskPublisher(for: createURLRequest(url:url))
            .tryMap { response -> Data in
                guard
                    let httpURLResponse = response.response as? HTTPURLResponse,
                    httpURLResponse.statusCode == 200
                else {
                    throw TeststationError.statusCode
                }
                
                sleep(1)
                
                return response.data
            }
            .mapError { TeststationError.map($0) }
            .eraseToAnyPublisher()
    }
    
    
    static func getPassInfo(serialNumber: String) throws -> AnyPublisher<PassInfo, TeststationError> {
        let url = URL(string: "\(EnvConfig.contagioapiBaseURL)/pass/info/\(serialNumber)")!
        
        return createUrlSession()
            .dataTaskPublisher(for: createURLRequest(url:url))
            .tryMap { response -> Data in
                guard
                    let httpURLResponse = response.response as? HTTPURLResponse,
                    httpURLResponse.statusCode == 200
                else {
                    throw TeststationError.statusCode
                }
                
                return response.data
            }
            .decode(type: PassInfo.self, decoder: createJsonDecoder())
            .mapError { TeststationError.map($0) }
            .eraseToAnyPublisher()
    }
    
    static func createPassInfo(createPassRequest: CreatePassRequest) throws -> AnyPublisher<CreatePassResponse, TeststationError> {
        let url = URL(string: "\(EnvConfig.contagioapiBaseURL)/pass/info")!
        
        var urlRequest = createURLRequest(url:url)
        
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
            .decode(type: CreatePassResponse.self, decoder: createJsonDecoder())
            .mapError { TeststationError.map($0) }
            .eraseToAnyPublisher()
    }
    
    static func updatePassInfo(updatePassRequest: UpdatePassRequest) throws -> AnyPublisher<PassInfo, TeststationError> {
        let url = URL(string: "\(EnvConfig.contagioapiBaseURL)/pass/info")!
        
        var urlRequest = createURLRequest(url:url)
        urlRequest.httpMethod = "PATCH"
        urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
        let payload = try createJsonEncoder().encode(updatePassRequest)
        
        print("updatePass() payload=\(String(decoding: payload, as: UTF8.self))")
        urlRequest.httpBody = payload
        
        return createUrlSession()
            .dataTaskPublisher(for: urlRequest)
            .tryMap { response -> Data in
                guard
                    let httpURLResponse = response.response as? HTTPURLResponse,
                    httpURLResponse.statusCode == 200
                else {
                    throw TeststationError.statusCode
                }
                
                print("updatePass() response.data=\(String(decoding: response.data, as: UTF8.self))")
                
                return response.data
            }
            .decode(type: PassInfo.self, decoder: createJsonDecoder())
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
        let result = JSONDecoder()
        result.dateDecodingStrategy = .formatted(DateFormatter.iso8601Full)
        
        return result;
    }
    
    
    private static func createJsonEncoder() ->JSONEncoder {
        let result = JSONEncoder()
        result.dateEncodingStrategy = .formatted(DateFormatter.iso8601Full)
        
        return result;
    }
    
    
    private static func createURLRequest(url:URL)  -> URLRequest {
        var urlRequest = URLRequest(url: url)
        
        guard let credentials = "\(EnvConfig.contagioapiUsername):\(EnvConfig.contagioapiPassword)".data(using: .utf8)?.base64EncodedString()
        else {
            return urlRequest
        }
        
        urlRequest.addValue("Basic \(credentials)", forHTTPHeaderField: "Authorization")
        
        return urlRequest
    }
    
}
