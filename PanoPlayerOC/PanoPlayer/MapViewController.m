//
//  MapViewController.m
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#import "MapViewController.h"
#import "PlayerViewController.h"
#import "SDWebImageDownloader.h"
#import "SDImageCache.h"

@interface MapViewController ()

@end

@implementation MapViewController
{
    UIScrollView         *_viewScrollStub;
    MTImageMapView       *_viewImageMap;
    NSArray              *_stateNames;
}
@synthesize viewScrollStub  = _viewScrollStub;
@synthesize viewImageMap    = _viewImageMap;
@synthesize stateNames      = _stateNames;
@synthesize responseData;
//@synthesize mapUrl;
@synthesize coordsData;
@synthesize linkScene;

-(NSString *)nibName
{
    return @"MapViewController";
}

- (void)loadView
{
    [super loadView];
	// Do any additional setup after loading the view, typically from a nib.
    
    //if(!responseData){
    responseData = [[NSString alloc] init];
    NSString *panoInfoUrl = [NSString stringWithFormat:@"http://beta1.yiluhao.com/ajax/m/pm/id/%@",  @"1"];
    NSLog(@"panoInfoUrl=%@", panoInfoUrl);
    responseData = [self getPanoMapFromUrl:panoInfoUrl];
    //}
    Boolean flag = false;
    
    if(responseData !=nil){
        NSDictionary *resultsDictionary = [responseData objectFromJSONString];
        coordsData = [resultsDictionary objectForKey:@"coords"];
        
        NSString *mapUrl = [resultsDictionary objectForKey:@"map"];
        
        if(coordsData && mapUrl){
            [self downLoadImage:mapUrl];
        }
        
        flag = true;
    }    
    if(!flag){
        [self getWrong:@"加载数据出错"];
    }
    
}

//下载图片
-(void)downLoadImage:(NSString *)url{
    //SDWebImageManager *manager = [SDWebImageManager sharedManager];
    NSURL *mapUrl = [NSURL URLWithString:url];
    NSLog(@"url=%@\n", url);
    //UIImage *cachedImage = [manager imageWithURL:faceUrl]; // 将需要缓存的图片加载进来
    UIImage *cachedImage = [[SDImageCache sharedImageCache] imageFromKey:url];
    
    if (cachedImage) {
        NSLog(@"cached");
        //cachedImage
        [self displayMap:cachedImage];
        
    } else {
        NSLog(@"nocached");
        // 如果Cache没有命中，则去下载指定网络位置的图片，并且给出一个委托方法
        [SDWebImageDownloader downloaderWithURL:mapUrl delegate:self userInfo:@"0"];
    }
}

- (void)imageDownloader:(SDWebImageDownloader *)downloader 
     didFinishWithImage:(UIImage *)image {
    [downloader retain];
    if(image == nil){
        [self getWrong:@"加载素材出错"];
    }    
    [[SDImageCache sharedImageCache] storeImage:image imageData:downloader.imageData forKey:[downloader.url absoluteString] toDisk:true];
    NSLog(@"face=%@", downloader.userInfo);
    
    //UIImage *mapImage = [[SDImageCache sharedImageCache] imageFromKey:[downloader.url absoluteString]];
    //[image release],image=nil;
    //[downloader release], downloader = nil;
    //[self displayMap:mapImage];
    [self loadView];
    
}


- (void) getWrong:(NSString*)str{
    NSString *msg = [NSString stringWithFormat:@"%@", str];
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil message:msg delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil, nil];
    [alert show];
    [alert release];
}

/**
 获取地图信息
 */
-(NSString *)getPanoMapFromUrl:(NSString *)url{
    NSURL *nsurl = [NSURL URLWithString:url];
    NSMutableURLRequest *theRequest = [[NSMutableURLRequest alloc] initWithURL:nsurl cachePolicy:NSURLRequestReloadIgnoringLocalCacheData timeoutInterval:60];
    //responseData = [[NSMutableData alloc] init];
    
    [theRequest addValue:@"gzip,deflate" forHTTPHeaderField:@"Accept-Encoding"];
    [theRequest addValue:@"*/*" forHTTPHeaderField:@"Accept"];
    [theRequest addValue:@"Mozilla/5.0 AppleWebKit/533.1 (KHTML, like Gecko)" forHTTPHeaderField:@"User-Agent"];
    //[theRequest addValue:@"http://www.cnbeta.com/" forHTTPHeaderField:@"Referer"];
    //[theRequest addValue:@"zh-cn" forHTTPHeaderField:@"Accept-Language"];
    
    //NSString *responseData = [[NSString alloc] init];
    NSError* error = nil;
    
    NSData *requestData = [NSURLConnection sendSynchronousRequest:theRequest returningResponse:nil error:&error];
    if(error != nil){
        [self getWrong:[NSString stringWithFormat:@"获取服务器数据失败\n%@",[error localizedDescription]]];
        return nil;
    }
    responseData = [[[NSString alloc]initWithData:requestData encoding:NSUTF8StringEncoding]autorelease];
    [theRequest release];
    return responseData;}

/**
 显示地图
 */
-(void)displayMap:(UIImage *)mapImage{
    
    NSMutableArray *arrStates = [[NSMutableArray alloc] init];
    linkScene = [[NSMutableArray alloc] init];
    UIImage *logo = [UIImage imageNamed:@"camera1.png"];
    
    for (int i=0; i<coordsData.count; i++) {
        NSDictionary  *tmp = [coordsData objectAtIndex:i];
        NSString *coords = [tmp objectForKey:@"coords"];
        NSString *linkId = [tmp objectForKey:@"linkScene"];
        [arrStates addObject:coords];
        [linkScene addObject:linkId];
        NSArray *aArray = [coords componentsSeparatedByString:@","];
        float marginLeft = [[aArray objectAtIndex:0] floatValue];
        float marginTop = [[aArray objectAtIndex:1] floatValue];
        //if(i==3){
        //添加水印
        mapImage = [self addImageLogo:mapImage waterMark:logo left:marginLeft top:marginTop];
        //}
    }
    [logo release], logo=nil;
    
    _viewImageMap.image = mapImage;
    [_viewImageMap sizeToFit];
    
    [_viewScrollStub addSubview:_viewImageMap];
    [_viewScrollStub setContentSize:
     [_viewImageMap sizeThatFits:CGSizeZero]
	 ];
    
    [_viewImageMap
     setMapping:arrStates
     doneBlock:^(MTImageMapView *imageMapView) {
         NSLog(@"Areas are all mapped");
     }];
    
}

-(UIImage *)addImageLogo:(UIImage *)img waterMark:(UIImage *)logo left:(float)left top:(float)top
{
    //get image width and height
    int w = img.size.width;
    int h = img.size.height;
    int logoWidth = logo.size.width;
    int logoHeight = logo.size.height;
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    
    //create a graphic context with CGBitmapContextCreate
    CGContextRef context = CGBitmapContextCreate(NULL, w, h, 8, 4 * w, colorSpace, kCGImageAlphaPremultipliedFirst);
    CGContextDrawImage(context, CGRectMake(0, 0, w, h), img.CGImage);
    
    NSLog(@"ls-ts=%d-%d", w, h);
    NSLog(@"la-ta=%d-%d", logoWidth, logoHeight);
    NSLog(@"l-t=%f-%f", left, top);
    NSLog(@"L-T=%f-%f", left+logoWidth/2, h-top);
    
    CGContextDrawImage(context, CGRectMake(left+15.0f, h-top-25.0f-logoHeight, logoWidth, logoHeight), [logo CGImage]);
    
    CGImageRef imageMasked = CGBitmapContextCreateImage(context);
    CGContextRelease(context);
    CGColorSpaceRelease(colorSpace);
    return [UIImage imageWithCGImage:imageMasked];
    //  CGContextDrawImage(contextRef, CGRectMake(100, 50, 200, 80), [smallImg CGImage]);
}

-(void)imageMapView:(MTImageMapView *)inImageMapView
   didSelectMapArea:(NSUInteger)inIndexSelected
{
    /*
     [[[[UIAlertView alloc]
     initWithTitle:@"*** State Name ***"
     message:[_stateNames objectAtIndex:inIndexSelected]
     delegate:nil
     cancelButtonTitle:@"Ok"
     otherButtonTitles:nil]
     autorelease] show];*/
    
    //NSDictionary *panoInfo = [panoList objectAtIndex:indexPath.row];
    NSString *panoId = [linkScene objectAtIndex:inIndexSelected];
    //NSLog(@"idddd=%@", panoId);
    
    PlayerViewController *playerView = [[PlayerViewController alloc] init];
    playerView.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:playerView animated:YES];
    
    NSDictionary *dic=[[NSDictionary alloc] initWithObjectsAndKeys:panoId,@"panoId", nil];
    //NSDictionary
    
    //发送消息.@"pass"匹配通知名，object:nil 通知类的范围  
    [[NSNotificationCenter defaultCenter] postNotificationName:@"panoId" object:nil userInfo:dic];  
    
    playerView.title = @"player";
    
    [playerView release];
    
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

@end