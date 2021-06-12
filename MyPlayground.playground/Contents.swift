import UIKit
import Combine

class bla : NSObject {
    
}

extension bla : URLSessionDelegate{
    public func urlSession(
        _ session: URLSession,
        didReceive challenge: URLAuthenticationChallenge,
        completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
        
        print("trust all certificates")
        
        //Trust the certificate even if not valid
        let urlCredential = URLCredential(trust: challenge.protectionSpace.serverTrust!)
        
        completionHandler(.useCredential, urlCredential)
    }
}

enum MyError: Error {
    case statusCode
    case encoding
    case decoding
    case invalidImage
    case invalidURL
    case other(Error)
    
    static func map(_ error: Error) -> MyError {
        print("TeststationError(error=\(error))")
        return (error as? MyError) ?? .other(error)
    }
}


var subscriptions = Set<AnyCancellable>()

func createURLRequest(url:URL)  -> URLRequest {
    var urlRequest = URLRequest(url: url)
    
    guard let credentials = "conapi1:conapi123".data(using: .utf8)?.base64EncodedString()
    else {
        return urlRequest
    }
    
    urlRequest.addValue("Basic \(credentials)", forHTTPHeaderField: "Authorization")
    
    return urlRequest
}

func createUrlSession() -> URLSession {
    let config = URLSessionConfiguration.default
    config.requestCachePolicy = .reloadIgnoringLocalCacheData
    config.urlCache = nil
    
    return URLSession(configuration: config, delegate: bla(), delegateQueue: nil)
}


let ids = ["123","4fdsf","4452","234"]

ids.publisher
    .sink(
        receiveCompletion: {
            completion in
            
            print(completion)
        },
        receiveValue: {
            result in
            
            print(result)
        }
    )
    .store(in: &subscriptions)

let url = URL(string: "https://efeu.local:13013/co_v1/teststations")

createUrlSession()
    .dataTaskPublisher(for: createURLRequest(url:url!))
    .tryMap { response -> Data in
        guard
            let httpURLResponse = response.response as? HTTPURLResponse,
            httpURLResponse.statusCode == 200
        else {
            let httpURLResponse = response.response as? HTTPURLResponse
            
            print("statusCode=\(String(describing: httpURLResponse?.statusCode))")
            throw MyError.statusCode
        }
        
        return response.data
    }
    .eraseToAnyPublisher()
    .sink(
        receiveCompletion: { completion in
            switch completion {
            case .finished:
                break
            case .failure(let error):
                print("Error: \(error)")
                break
            }
        },
        receiveValue: {
            result in
            
            let str = String(decoding: result, as: UTF8.self)
            print(str)
        }
    )
    .store(in: &subscriptions)
